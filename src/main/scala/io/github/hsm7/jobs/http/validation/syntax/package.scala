package io.github.hsm7.jobs.http.validation.syntax

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.implicits.*
import io.github.hsm7.jobs.domain.{ErrorResult, InvalidRequest, ValidationErrors}
import org.http4s.{EntityDecoder, Request}
import io.github.hsm7.jobs.http.validation.Validator
import org.typelevel.log4cats.Logger

extension [F[_]: MonadThrow: Logger](request: Request[F])
  def validate[A: Validator](using EntityDecoder[F, A]): F[Either[ErrorResult, A]] =
    request
      .as[A]
      .attempt
      .map {
        case Left(_)       => Left(InvalidRequest("Invalid request body."))
        case Right(entity) => Validator[A].validate(entity).leftMap(errorsMapper).toEither
      }

private def errorsMapper[F[_]: MonadThrow: Logger, A: Validator](nel: NonEmptyList[(String, String)]) =
  ValidationErrors(nel.toList.toMap)
