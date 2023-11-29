package io.github.hsm7.jobs.modules

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import io.github.hsm7.jobs.http.routes.{HealthRoutes, JobRoutes}
import org.http4s.HttpRoutes
import org.http4s.server.Router

class Http[F[_]: Async] private (services: Services[F]) {

  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes    = JobRoutes[F](services.jobs).routes

  val routes: HttpRoutes[F] = Router(
    "/api" -> (healthRoutes <+> jobRoutes)
  )
}

object Http {
  def apply[F[_]: Async](services: Services[F]): Resource[F, Http[F]] = Resource.pure(new Http[F](services))
}
