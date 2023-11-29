package io.github.hsm7.jobs.modules

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import io.github.hsm7.jobs.http.resources.{HealthResource, JobResource}
import org.http4s.HttpRoutes
import org.http4s.server.Router

final class HttpResources[F[_]: Async] private(val routes: HttpRoutes[F])

object HttpResources {
  def apply[F[_]: Async](services: Services[F]): Resource[F, HttpResources[F]] =
    val healthRoutes = HealthResource[F].routes
    val jobRoutes    = JobResource[F](services.jobs).routes

    val routes: HttpRoutes[F] = Router(
      "/api" -> (healthRoutes <+> jobRoutes)
    )

    Resource.pure(new HttpResources[F](routes))
}
