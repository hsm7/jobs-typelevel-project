package io.github.hsm7.jobs.domain

case class Pagination(offset: Int, limit: Int)

object Pagination {
  def apply(maybeOffset: Option[Int], maybeLimit: Option[Int]): Pagination =
    new Pagination(maybeOffset.getOrElse(0), maybeLimit.getOrElse(20))
}
