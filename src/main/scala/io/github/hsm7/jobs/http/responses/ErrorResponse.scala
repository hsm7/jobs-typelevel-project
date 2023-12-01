package io.github.hsm7.jobs.http.responses

enum ErrorResponse {
  case InvalidRequest(message: String)
  case ResourceNotFound(message: String)
  case ValidationErrors(messages: Map[String, String])
}
