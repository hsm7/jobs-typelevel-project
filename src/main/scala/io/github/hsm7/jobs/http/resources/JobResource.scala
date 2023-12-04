package io.github.hsm7.jobs.http.resources

import cats.data.{EitherT, ValidatedNel}
import cats.effect.Async
import cats.implicits.*
import io.circe.generic.auto.*
import io.github.hsm7.jobs.domain.{ErrorResult, Pagination}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.*
import org.http4s.{HttpRoutes, ParseFailure, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID
import io.github.hsm7.jobs.domain.job.{Job, JobFilters, JobInfo}
import io.github.hsm7.jobs.http.utils.ErrorResultMapper
import io.github.hsm7.jobs.http.validation.syntax.*
import io.github.hsm7.jobs.http.validation.validators.given
import io.github.hsm7.jobs.services.Jobs

class JobResource[F[_]: Async] private (jobs: Jobs[F]) extends Http4sDsl[F] {

  given logger: Logger[F] = Slf4jLogger.getLogger[F]

  private object OffsetQueryParam      extends OptionalQueryParamDecoderMatcher[Int]("offset")
  private object LimitQueryParam       extends OptionalQueryParamDecoderMatcher[Int]("limit")
  private object TagsQueryParam        extends OptionalMultiQueryParamDecoderMatcher[String]("tag")
  private object CompaniesQueryParam   extends OptionalMultiQueryParamDecoderMatcher[String]("company")
  private object LocationsQueryParam   extends OptionalMultiQueryParamDecoderMatcher[String]("location")
  private object CountriesQueryParam   extends OptionalMultiQueryParamDecoderMatcher[String]("country")
  private object SenioritiesQueryParam extends OptionalMultiQueryParamDecoderMatcher[String]("seniority")
  private object MaxSalaryQueryParam   extends OptionalQueryParamDecoderMatcher[Int]("salary")
  private object RemoteQueryParam      extends OptionalQueryParamDecoderMatcher[Boolean]("remote")

  private def extractQueryParams(validated: ValidatedNel[ParseFailure, List[String]]): List[String] =
    validated.fold(_ => List.empty, identity)

  // get all jobs
  private val getAll: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? OffsetQueryParam(offset) +& LimitQueryParam(limit) +& TagsQueryParam(tags) +&
        CompaniesQueryParam(companies) +& LocationsQueryParam(locations) +& CountriesQueryParam(countries) +&
        SenioritiesQueryParam(seniorities) +& MaxSalaryQueryParam(maxSalary) +& RemoteQueryParam(remote) =>
      val pagination: Pagination = Pagination(offset, limit)
      val jobFilters: JobFilters = JobFilters(
        extractQueryParams(companies),
        extractQueryParams(locations),
        extractQueryParams(countries),
        extractQueryParams(seniorities),
        extractQueryParams(tags),
        maxSalary,
        remote
      )
      jobs.getAll(pagination, jobFilters).flatMap { jobs =>
        Ok(jobs) <* logger.info(s"Found ${jobs.size} jobs.")
      }
  }

  // get job by id
  private val get: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobs.get(id).flatMap {
      case Left(e: ErrorResult) => ErrorResultMapper.makeResponse(e)
      case Right(job: Job)      => Ok(job) <* logger.info(s"Found job with id: $id")
    }
  }

  // create new job
  private val create: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    val transformer = for {
      jobInfo  <- EitherT(req.validate[JobInfo])
      response <- EitherT.liftF(jobs.create("email@example.com", jobInfo))
    } yield response

    transformer.value.flatMap {
      case Left(e: ErrorResult) => ErrorResultMapper.makeResponse(e)
      case Right(id: UUID)      => Created(id) <* logger.info(s"Created job with id: $id")
    }
  }

  // update job
  private val update: HttpRoutes[F] = HttpRoutes.of[F] { case req @ PUT -> Root / UUIDVar(id) =>
    val transformer = for {
      jobInfo  <- EitherT(req.validate[JobInfo])
      response <- EitherT(jobs.update(id, jobInfo))
    } yield response

    transformer.value.flatMap {
      case Left(e: ErrorResult) => ErrorResultMapper.makeResponse(e)
      case Right(id)            => NoContent() <* logger.info(s"Updated job with id: $id")
    }
  }

  // delete job
  private val delete: HttpRoutes[F] = HttpRoutes.of[F] { case DELETE -> Root / UUIDVar(id) =>
    jobs.delete(id).flatMap { _ =>
      NoContent() <* logger.info(s"Deleted job with id: $id")
    }
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (getAll <+> get <+> create <+> update <+> delete)
  )
}

object JobResource {
  def apply[F[_]: Async](jobs: Jobs[F]): JobResource[F] = new JobResource[F](jobs)
}
