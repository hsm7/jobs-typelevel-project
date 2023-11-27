package io.github.hsm7.playground

import cats.Monad
import cats.implicits.*
import cats.data.ValidatedNel
import cats.effect.{IO, IOApp}
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.circe.*
import org.http4s.{Header, HttpRoutes, ParseFailure}
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.ci.CIString

import java.util.UUID

object Http4sPlayground extends IOApp.Simple {

  type Student = String

  case class Instructor(firstName: String, lastName: String)
  case class Course(id: String, title: String, year: Int, students: List[Student], instructor: String)

  private object Courses {
    private val typelevelCourse: Course = Course(
      "b48650a8-8d53-11ee-b9d1-0242ac120002",
      "Typelevel Rite of Passage",
      2023,
      List("Frodo", "Sam"),
      "Martin Odersky"
    )

    private val courses: Map[String, Course] = Map(typelevelCourse.id -> typelevelCourse)

    def findCourseById(courseId: UUID): Option[Course] = courses.get(courseId.toString)
    def findCoursesByInstructor(name: String): List[Course] = courses.values.filter(_.instructor == name).toList
  }

  private object InstructorQueryParamMatcher extends QueryParamDecoderMatcher[String]("instructor")
  private object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Int]("year")

  private def courseRoutes[F[_]: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*

    def filterByYear(courses: List[Course], validated: ValidatedNel[ParseFailure, Int]) = {
      validated.fold(
        _ => BadRequest("Query parameter 'year' is invalid"),
        year => Ok(courses.filter(_.year == year).asJson)
      )
    }

    HttpRoutes.of[F] {
      case GET -> Root / "courses" :? InstructorQueryParamMatcher(instructor) +& YearQueryParamMatcher(maybeYear) =>
        val courses = Courses.findCoursesByInstructor(instructor)
        maybeYear match {
          case Some(validated) => filterByYear(courses, validated)
          case None => Ok(courses.asJson)
        }
      case GET -> Root / "courses" / UUIDVar(courseId) / "students" =>
        Courses.findCourseById(courseId).map(_.students) match
          case Some(students) => Ok(students.asJson, Header.Raw(CIString("custom-header"), "custom-header-value"))
          case None => NotFound(s"No course with $courseId was found.")
    }
  }

  private def healthRoute[F[_]: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "health" => Ok("All is ok")
    }
  }

  private def allRoutes[F[_]: Monad]: HttpRoutes[F] = courseRoutes[F] <+> healthRoute[F]


  override def run: IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(allRoutes[IO].orNotFound)
    .build
    .use(_ => IO.never)
}
