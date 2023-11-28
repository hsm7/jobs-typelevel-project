package io.github.hsm7.jobs.http.routes

import cats.effect.kernel.Async
import cats.implicits.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.collection.mutable
import io.github.hsm7.jobs.domain.job.{Job, JobInfo}
import io.github.hsm7.jobs.http.utils.responses.ErrorResponse
import io.github.hsm7.jobs.logging.syntax.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

class JobRoutes[F[_]: Async] private extends Http4sDsl[F] {

  given logger: Logger[F] = Slf4jLogger.getLogger[F]

  private val jobs = mutable.Map[UUID, Job]()

  // get all jobs
  private val getAll: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      jobs     <- jobs.values.pure[F]
      response <- Ok(jobs)
      _        <- logger.info(s"Found ${jobs.size} jobs.")
    } yield response
  }

  // get job by id
  private val get: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobs.get(id) match {
      case None => logger.info(s"No job found with id: $id") >> NotFound(ErrorResponse(s"No job found with id: $id"))
      case Some(job) => logger.info(s"Found job with id: $id") >> Ok(job)
    }
  }

  // create new job
  private val create: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    for {
      jobInfo  <- req.as[JobInfo].logError(e => s"Parsing request failed with: $e")
      job      <- createJob(jobInfo).pure[F]
      _        <- jobs.put(job.id, job).pure[F]
      response <- Created(job.id)
      _        <- logger.info(s"Created job with id: ${job.id}")
    } yield response
  }

  private def createJob(jobInfo: JobInfo): Job = Job(
    id = UUID.randomUUID(),
    date = System.currentTimeMillis(),
    ownerEmail = "owner@example.com",
    jobInfo = jobInfo,
    active = true
  )

  // update job
  private val update: HttpRoutes[F] = HttpRoutes.of[F] { case req @ PUT -> Root / UUIDVar(id) =>
    jobs.get(id) match
      case None => logger.info(s"No job found with id: $id") >> NotFound(ErrorResponse(s"No job found with id: $id"))
      case Some(job) =>
        for {
          jobInfo  <- req.as[JobInfo].logError(e => s"Parsing request failed with: $e")
          _        <- jobs.put(id, job.copy(jobInfo = jobInfo)).pure[F]
          response <- NoContent()
          _        <- logger.info(s"Updated job with id: ${job.id}")
        } yield response
  }

  // delete job
  private val delete: HttpRoutes[F] = HttpRoutes.of[F] { case DELETE -> Root / UUIDVar(id) =>
    jobs.get(id) match
      case None => logger.info(s"No job found with id: $id") >> NotFound(ErrorResponse(s"No job found with id: $id"))
      case Some(_) =>
        for {
          _        <- jobs.remove(id).pure[F]
          response <- NoContent()
          _        <- logger.info(s"Deleted job with id: $id")
        } yield response
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (getAll <+> get <+> create <+> update <+> delete)
  )
}

object JobRoutes {
  def apply[F[_]: Async]: JobRoutes[F] = new JobRoutes[F]
}
