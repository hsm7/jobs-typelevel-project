package io.github.hsm7.jobs.http.resources

import cats.effect.Async
import cats.implicits.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID
import io.github.hsm7.jobs.domain.job.{Job, JobInfo}
import io.github.hsm7.jobs.http.utils.responses.ErrorResponse
import io.github.hsm7.jobs.logging.syntax.*
import io.github.hsm7.jobs.services.Jobs

class JobResource[F[_]: Async] private(jobs: Jobs[F]) extends Http4sDsl[F] {

  given logger: Logger[F] = Slf4jLogger.getLogger[F]

  // get all jobs
  private val getAll: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    jobs.getAll.flatMap { jobs =>
      logger.info(s"Found ${jobs.size} jobs.") >> Ok(jobs)
    }
  }

  // get job by id
  private val get: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobs.get(id).flatMap {
      case None => logger.info(s"No job found with id: $id") >> NotFound(ErrorResponse(s"No job found with id: $id"))
      case Some(job) => logger.info(s"Found job with id: $id") >> Ok(job)
    }
  }

  // create new job
  private val create: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    for {
      jobInfo  <- req.as[JobInfo].logError(e => s"Parsing request failed with: $e")
      id       <- jobs.create("email@example.com", jobInfo)
      response <- Created(id)
      _        <- logger.info(s"Created job with id: $id")
    } yield response
  }

  // update job
  private val update: HttpRoutes[F] = HttpRoutes.of[F] { case req @ PUT -> Root / UUIDVar(id) =>
    for {
      jobInfo <- req.as[JobInfo].logError(e => s"Parsing request failed with: $e")
      updated <- jobs.update(id, jobInfo)
      response <-
        if updated then logger.info(s"Updated job with id: $id") >> NoContent()
        else logger.info(s"No job found with id: $id") >> NotFound(ErrorResponse(s"No job found with id: $id"))
    } yield response
  }

  // delete job
  private val delete: HttpRoutes[F] = HttpRoutes.of[F] { case DELETE -> Root / UUIDVar(id) =>
    jobs.delete(id).flatMap { deleted =>
      if deleted then logger.info(s"Deleted job with id: $id") >> NoContent()
      else logger.info(s"No job found with id: $id") >> NoContent()
    }
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (getAll <+> get <+> create <+> update <+> delete)
  )
}

object JobResource {
  def apply[F[_]: Async](jobs: Jobs[F]): JobResource[F] = new JobResource[F](jobs)
}