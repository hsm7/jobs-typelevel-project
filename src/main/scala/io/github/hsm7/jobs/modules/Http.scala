package io.github.hsm7.jobs.modules

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import io.github.hsm7.jobs.http.routes.{HealthRoutes, JobRoutes}
import org.http4s.HttpRoutes
import org.http4s.server.Router

final class Http[F[_]: Async] private (val routes: HttpRoutes[F])

object Http {
  def apply[F[_]: Async](services: Services[F]): Resource[F, Http[F]] =
    val healthRoutes = HealthRoutes[F].routes
    val jobRoutes    = JobRoutes[F](services.jobs).routes

    val routes: HttpRoutes[F] = Router(
      "/api" -> (healthRoutes <+> jobRoutes)
    )

    Resource.pure(new Http[F](routes))
}
