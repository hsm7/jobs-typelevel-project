package io.github.hsm7.jobs.modules

import cats.effect.kernel.{Async, Resource}
import io.github.hsm7.jobs.services.{Jobs, JobService}

final class Services[F[_]] private (val jobs: Jobs[F])

object Services {

  def apply[F[_]: Async](database: Database[F]): Resource[F, Services[F]] =
    JobService[F](database.xa)
      .map(jobs => new Services(jobs))
}
