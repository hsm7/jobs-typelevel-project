package io.github.hsm7.jobs.services

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import io.github.hsm7.jobs.domain.ResourceNotFound
import io.github.hsm7.jobs.fixtures.JobFixture
import io.github.hsm7.jobs.services.utils.Database
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class JobsSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with JobFixture {

  val initScriptPath: String = "sql/jobs.sql"
  val jobsResource: Resource[IO, JobService[IO]] = for {
    postgresContainer <- Database.makePostgresContainer[IO](initScriptPath)
    xa                <- Database.makeTransactor[IO](postgresContainer)
    jobService        <- JobService[IO](xa)
  } yield jobService

  "Jobs service" - {
    "Should return nothing if the given ID doesn't exist" in {
      jobsResource.use { jobs =>
        jobs.get(NOT_FOUND_JOB_UUID).map(_ shouldBe Left(ResourceNotFound(JOB_NOT_FOUND_PREFIX + NOT_FOUND_JOB_UUID)))
      }
    }

    "Should return a job for the given ID" in {
      jobsResource.use { jobs =>
        jobs.get(SCALA_JOB_UUID).map(_ shouldBe Right(SCALA_JOB))
      }
    }

    "Should retrieve all jobs" in {
      jobsResource.use { jobs =>
        jobs.getAll.map(_ shouldBe List(SCALA_JOB))
      }
    }

    "Should create a new job" in {
      jobsResource.use { jobs =>
        for {
          id  <- jobs.create(ROCK_THE_JVM_SCALA_JOB.ownerEmail, ROCK_THE_JVM_JOB)
          job <- jobs.get(id)
        } yield job.map(_.jobInfo) shouldBe Right(ROCK_THE_JVM_JOB)
      }
    }

    "Should update a job" in {
      jobsResource.use { jobs =>
        for {
          id  <- jobs.update(SCALA_JOB_UUID, SCALA_JOB_UPDATED.jobInfo)
          job <- jobs.get(SCALA_JOB_UUID)
        } yield (job, id) shouldBe (Right(SCALA_JOB_UPDATED), Right(SCALA_JOB_UUID))
      }
    }

    "Should not update a non existing job" in {
      jobsResource.use { jobs =>
        for {
          id  <- jobs.update(NOT_FOUND_JOB_UUID, SCALA_JOB_UPDATED.jobInfo)
          job <- jobs.get(SCALA_JOB_UUID)
        } yield (job, id) shouldBe (Right(SCALA_JOB), Left(ResourceNotFound(JOB_NOT_FOUND_PREFIX + NOT_FOUND_JOB_UUID)))
      }
    }

    "Should delete a job" in {
      jobsResource.use { jobs =>
        for {
          _   <- jobs.delete(SCALA_JOB_UUID)
          job <- jobs.get(SCALA_JOB_UUID)
        } yield job shouldBe Left(ResourceNotFound(JOB_NOT_FOUND_PREFIX + SCALA_JOB_UUID))
      }
    }
  }
}
