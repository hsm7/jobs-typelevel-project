package io.github.hsm7.jobs.http.validation

import cats.data.ValidatedNel

trait Validator[A] {
  def validate(value: A): ValidatedNel[(String, String), A]
}
object Validator {
  def apply[A: Validator]: Validator[A] = summon[Validator[A]]
}
