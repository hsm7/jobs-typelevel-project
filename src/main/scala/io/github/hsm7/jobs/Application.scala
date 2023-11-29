package io.github.hsm7.jobs

import cats.effect.{IO, IOApp, Resource}
import pureconfig.ConfigSource
import org.http4s.server.Server

import io.github.hsm7.jobs.config.syntax.*
import io.github.hsm7.jobs.config.ApplicationConfig
import io.github.hsm7.jobs.modules.*

object Application extends IOApp.Simple {

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, ApplicationConfig].flatMap { case ApplicationConfig(dbConfig, serverConfig) =>
      val appResource: Resource[IO, Server] = for {
        database      <- Database[IO](dbConfig)
        services      <- Services[IO](database)
        httpResources <- HttpResources[IO](services)
        httpServer    <- HttpServer[IO](serverConfig, httpResources)
      } yield httpServer.server

      appResource.use(_ => IO.never)
    }

}
