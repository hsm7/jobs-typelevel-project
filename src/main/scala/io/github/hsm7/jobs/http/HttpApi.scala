package io.github.hsm7.jobs.http

import cats.effect.kernel.Async
import cats.implicits.*
import io.github.hsm7.jobs.http.routes.{HealthRoutes, JobRoutes}
import org.http4s.HttpRoutes
import org.http4s.server.Router

class HttpApi[F[_]: Async] private {

  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes    = JobRoutes[F].routes

  val routes: HttpRoutes[F] = Router(
    "/api" -> (healthRoutes <+> jobRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Async]: HttpApi[F] = new HttpApi[F]
}
