package io.github.hsm7.jobs.http.utils

object responses {
  sealed trait ErrorResponse
  final case class SingleErrorResponse(error: String)       extends ErrorResponse
  final case class MultiErrorResponse(errors: List[String]) extends ErrorResponse

  object ErrorResponse {
    def apply(error: String): SingleErrorResponse       = SingleErrorResponse(error)
    def apply(errors: List[String]): MultiErrorResponse = MultiErrorResponse(errors)
  }
}
