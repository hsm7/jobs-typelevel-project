package io.github.hsm7.jobs.http.resources

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class HealthResource[F[_]: Monad] private extends Http4sDsl[F] {
  private val healthRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok("Up and running.")
  }

  val routes: HttpRoutes[F] = Router(
    "health" -> healthRoute
  )
}

object HealthResource {
  def apply[F[_]: Monad]: HealthResource[F] = new HealthResource[F]
}
