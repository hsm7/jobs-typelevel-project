package io.github.hsm7.jobs.http.resources

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import io.github.hsm7.jobs.fixtures.JobFixture
import io.github.hsm7.jobs.domain.job.{Job, JobFilters, JobInfo}
import io.github.hsm7.jobs.domain.{ErrorResult, Pagination}
import io.github.hsm7.jobs.services.Jobs
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.implicits.*

import java.util.UUID

class JobResourceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] with JobFixture {

  val jobsMock: Jobs[IO] = new Jobs[IO] {
    override def getAll: IO[List[Job]] = IO.pure(List(SCALA_JOB))
    override def getAll(pagination: Pagination, filters: JobFilters): IO[List[Job]] =
      IO.pure(List(SCALA_JOB))

    override def get(id: UUID): IO[Either[ErrorResult, Job]] =
      if (id == SCALA_JOB_UUID) IO.pure(Right(SCALA_JOB))
      else IO.pure(Left(JOB_NOT_FOUND_ERROR))

    override def create(ownerEmail: String, jobInfo: JobInfo): IO[UUID] = IO.pure(NEW_JOB_UUID)

    override def update(id: UUID, jobInfo: JobInfo): IO[Either[ErrorResult, UUID]] =
      if (id == SCALA_JOB_UUID) IO.pure(Right(id))
      else IO.pure(Left(JOB_NOT_FOUND_ERROR))

    override def delete(id: UUID): IO[Unit] = IO.unit
  }

  val jobRoutes: HttpRoutes[IO] = JobResource[IO](jobsMock).routes

  "JobResource" - {
    "Should return a job with a given ID" in {
      val location = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064"
      for {
        response <- jobRoutes.orNotFound.run(Request(method = Method.GET, uri = location))
        job      <- response.as[Job]
      } yield {
        response.status shouldBe Status.Ok
        job shouldBe SCALA_JOB
      }
    }

    "Should not find a non existing job" in {
      val location = uri"/jobs/6ea79557-3112-4c84-a8f5-1d1e2c300948"
      for {
        response <- jobRoutes.orNotFound.run(Request(method = Method.GET, uri = location))
      } yield response.status shouldBe Status.NotFound
    }

    "Should return all jobs" in {
      val location = uri"/jobs"
      for {
        response <- jobRoutes.orNotFound.run(Request(method = Method.GET, uri = location))
        jobs     <- response.as[List[Job]]
      } yield {
        response.status shouldBe Status.Ok
        jobs shouldBe List(SCALA_JOB)
      }
    }

    "Should create new job" in {
      val location = uri"/jobs"
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.POST, uri = location).withEntity(ROCK_THE_JVM_SCALA_JOB.jobInfo)
        )
        uuid <- response.as[UUID]
      } yield {
        response.status shouldBe Status.Created
        uuid shouldBe NEW_JOB_UUID
      }
    }

    "Should update a job" in {
      val location = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064"
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = location).withEntity(SCALA_JOB_UPDATED.jobInfo)
        )
      } yield response.status shouldBe Status.NoContent
    }

    "Should not update a non existing job" in {
      val location = uri"/jobs/6ea79557-3112-4c84-a8f5-1d1e2c300948"
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = location).withEntity(SCALA_JOB_UPDATED.jobInfo)
        )
      } yield response.status shouldBe Status.NotFound
    }

    "Should delete a job" in {
      val location = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064"
      for {
        response <- jobRoutes.orNotFound.run(Request(method = Method.DELETE, uri = location))
      } yield response.status shouldBe Status.NoContent
    }
  }

}
