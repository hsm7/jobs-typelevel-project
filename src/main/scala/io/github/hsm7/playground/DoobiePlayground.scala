package io.github.hsm7.playground

import cats.effect.{IO, IOApp, MonadCancelThrow, Resource}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

object DoobiePlayground extends IOApp.Simple {

  private case class Student(id: Int, name: String)

  private trait Repository[F[_], A] {
    def findById(id: Int): F[Option[A]]
    def findAll: F[List[A]]
    def create(name: String): F[Int]
  }

  private object Students {

    def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): Repository[F, Student] = new Repository[F, Student] {

      override def findById(id: Int): F[Option[Student]] =
        sql"select id, name from students where id=$id"
          .query[Student]
          .option
          .transact(xa)

      override def findAll: F[List[Student]] =
        sql"select id, name from students"
          .query[Student]
          .to[List]
          .transact(xa)

      override def create(name: String): F[Int] =
        sql"insert into students(name) values ($name)".update
          .withUniqueGeneratedKeys[Int]("id")
          .transact(xa)
    }
  }

  private val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](16)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5433/demo",
      "docker",
      "docker",
      ec
    )
  } yield xa

  override def run: IO[Unit] = postgresResource.use { xa =>
    for {
      _        <- Students[IO](xa).create("hussam")
      _        <- Students[IO](xa).create("marshall")
      students <- Students[IO](xa).findAll
      _        <- IO.println(students)
    } yield ()
  }

}
