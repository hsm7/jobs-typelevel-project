package io.github.hsm7.jobs.domain

sealed trait ErrorResult {
  val message: String
}
case class InvalidRequest(message: String)   extends ErrorResult
case class ResourceNotFound(message: String) extends ErrorResult
case class ValidationErrors(messages: Map[String, String]) extends ErrorResult {
  override val message: String = messages
    .map { case (field, message) => field + ": " + message }
    .mkString(", ")
}
