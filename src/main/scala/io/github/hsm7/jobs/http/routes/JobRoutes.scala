package io.github.hsm7.jobs.http.routes

import cats.effect.Concurrent
import cats.implicits.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import java.util.UUID
import scala.collection.mutable
import io.github.hsm7.jobs.domain.job.{Job, JobInfo}
import io.github.hsm7.jobs.http.utils.responses.ErrorResponse

class JobRoutes[F[_]: Concurrent] private extends Http4sDsl[F] {

  private val jobs = mutable.Map[UUID, Job]()

  // get all jobs
  private val getAll: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(jobs.values)
  }

  // get job by id
  private val get: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobs.get(id) match
      case Some(job) => Ok(job)
      case None      => NotFound(ErrorResponse(s"No job found with id: $id"))
  }

  // create new job
  private val create: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    for {
      jobInfo  <- req.as[JobInfo]
      job      <- createJob(jobInfo)
      _        <- jobs.put(job.id, job).pure[F]
      response <- Created(job.id)
    } yield response
  }

  private def createJob(jobInfo: JobInfo): F[Job] = Job(
    id = UUID.randomUUID(),
    date = System.currentTimeMillis(),
    ownerEmail = "owner@example.com",
    jobInfo = jobInfo,
    active = true
  ).pure[F]

  // update job
  private val update: HttpRoutes[F] = HttpRoutes.of[F] { case req @ PUT -> Root / UUIDVar(id) =>
    jobs.get(id) match
      case Some(job) =>
        for {
          jobInfo  <- req.as[JobInfo]
          _        <- jobs.put(id, job.copy(jobInfo = jobInfo)).pure[F]
          response <- NoContent()
        } yield response
      case None => NotFound(ErrorResponse(s"No job found with id: $id"))
  }

  // delete job
  private val delete: HttpRoutes[F] = HttpRoutes.of[F] { case req @ DELETE -> Root / UUIDVar(id) =>
    jobs.get(id) match
      case Some(_) =>
        for {
          _        <- jobs.remove(id).pure[F]
          response <- NoContent()
        } yield response
      case None => NotFound(ErrorResponse(s"No job found with id: $id"))
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (getAll <+> get <+> create <+> update <+> delete)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent]: JobRoutes[F] = new JobRoutes[F]
}
