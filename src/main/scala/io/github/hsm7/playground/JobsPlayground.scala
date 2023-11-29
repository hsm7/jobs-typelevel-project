package io.github.hsm7.playground

import cats.effect.kernel.Resource
import cats.effect.{IO, IOApp, MonadCancelThrow}
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.util.ExecutionContexts
import io.github.hsm7.jobs.domain.job.JobInfo
import io.github.hsm7.jobs.services.JobsService

object JobsPlayground extends IOApp.Simple {

  private val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](16)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5433/postgres",
      "postgres",
      "postgres",
      ec
    )
  } yield xa

  private val jobInfo: JobInfo = JobInfo(
    title = "Fullstack Developer",
    company = "Ravenware",
    description = "A very long job description",
    externalUrl = "www.example.com",
    location = "KRT",
    remote = true
  )

  private val updateJob: JobInfo = JobInfo(
    title = "Senior Scala Developer",
    company = "Ravenware",
    description = "A very long job description",
    externalUrl = "www.example.com",
    location = "KRT",
    remote = false
  )

  override def run: IO[Unit] = postgresResource.use { xa =>
    for {
      jobs     <- JobsService[IO](xa)
      id       <- jobs.create("email@example.com", jobInfo)
      _        <- IO.println(s"Created job with id: $id")
      job      <- jobs.get(id)
      _        <- IO.println(s"Found job: $job")
      flag     <- jobs.update(id, updateJob)
      _        <- IO.println(s"Update flag: $flag")
      allJobs  <- jobs.getAll
      _        <- IO.println(s"All jobs: $allJobs")
      flag2    <- jobs.delete(id)
      _        <- IO.println(s"Delete flag: $flag2")
      allJobs2 <- jobs.getAll
      _        <- IO.println(s"All jobs: $allJobs2")
    } yield ()
  }

}
