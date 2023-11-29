package io.github.hsm7.jobs.modules

import cats.effect.{Async, Resource}
import io.github.hsm7.jobs.config.ServerConfig
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

class HttpServer[F[_]: Async] private (val server: Server)

object HttpServer {
  def apply[F[_]: Async](serverConf: ServerConfig, httpResources: HttpResources[F]): Resource[F, HttpServer[F]] =
    EmberServerBuilder
      .default[F]
      .withHttpApp(httpResources.routes.orNotFound)
      .withHost(serverConf.host)
      .withPort(serverConf.port)
      .build
      .map(server => new HttpServer[F](server))
}
