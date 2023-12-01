package io.github.hsm7.jobs.http.validation.syntax

import cats.MonadThrow
import cats.data.Validated
import cats.implicits.*
import org.http4s.{EntityDecoder, Request}
import io.github.hsm7.jobs.http.utils.responses.ErrorResponse
import io.github.hsm7.jobs.http.validation.Validator
import org.typelevel.log4cats.Logger

extension [F[_]: MonadThrow: Logger](request: Request[F])
  def validate[A: Validator](using EntityDecoder[F, A]): F[Validated[ErrorResponse, A]] =
    request
      .as[A]
      .attempt
      .map {
        case Left(_)       => Validated.invalid(ErrorResponse("Invalid request body."))
        case Right(entity) => Validator[A].validate(entity).leftMap(l => ErrorResponse(l.toList))
      }
