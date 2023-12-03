package io.github.hsm7.jobs.http.utils

import cats.Applicative
import cats.implicits.*
import io.circe.generic.auto.*
import io.github.hsm7.jobs.domain.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.*
import org.http4s.Response
import org.http4s.Status.{BadRequest, NotFound}
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

object ErrorResultMapper {
  def makeResponse[F[_] : Applicative : Logger](errorResponse: ErrorResult): F[Response[F]] =
    val dsl = Http4sDsl[F]
    import dsl.*
    errorResponse match
      case e: InvalidRequest => BadRequest(e) <* Logger[F].info(s"Bad Request: ${e.message}")
      case e: ValidationErrors => BadRequest(e) <* Logger[F].info(s"Validation error: ${e.message}")
      case e: ResourceNotFound => NotFound(e) <* Logger[F].info(s"Resource not found: ${e.message}")
}