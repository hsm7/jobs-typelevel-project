package io.github.hsm7.jobs.http

import cats.effect.Concurrent
import cats.implicits.*
import io.github.hsm7.jobs.http.routes.{HealthRoutes, JobRoutes}
import org.http4s.HttpRoutes
import org.http4s.server.Router

class HttpApi[F[_]: Concurrent] private {

  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes    = JobRoutes[F].routes

  val routes: HttpRoutes[F] = Router(
    "/api" -> (healthRoutes <+> jobRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent]: HttpApi[F] = new HttpApi[F]
}
