package io.github.hsm7.jobs

import cats.effect.{IO, IOApp, Resource}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import pureconfig.ConfigSource

import io.github.hsm7.jobs.config.syntax.*
import io.github.hsm7.jobs.config.{ApplicationConfig, ServerConfig}
import io.github.hsm7.jobs.modules.*

object Application extends IOApp.Simple {

  private def makeServerResource(serverConf: ServerConfig, http: Http[IO]): Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHttpApp(http.routes.orNotFound)
      .withHost(serverConf.host)
      .withPort(serverConf.port)
      .build

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, ApplicationConfig].flatMap { case ApplicationConfig(dbConf, serverConf) =>
      val appResource: Resource[IO, Server] = for {
        database <- Database[IO](dbConf)
        services <- Services[IO](database)
        http     <- Http[IO](services)
        server   <- makeServerResource(serverConf, http)
      } yield server

      appResource.use(_ => IO.never)
    }

}
