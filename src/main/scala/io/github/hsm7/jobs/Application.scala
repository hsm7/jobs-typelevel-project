package io.github.hsm7.jobs

import cats.effect.{IO, IOApp, Resource}
import pureconfig.ConfigSource
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import io.github.hsm7.jobs.config.syntax.*
import io.github.hsm7.jobs.config.ApplicationConfig
import io.github.hsm7.jobs.modules.*

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, ApplicationConfig].flatMap { case ApplicationConfig(dbConfig, serverConfig) =>
      val appResource: Resource[IO, Server] = for {
        database      <- Database[IO](dbConfig)
        services      <- Services[IO](database)
        httpResources <- HttpResources[IO](services)
        httpServer    <- HttpServer[IO](serverConfig, httpResources)
      } yield httpServer.server

      appResource.use { _ =>
        logger.info(s"Server is running @ ${serverConfig.host}:${serverConfig.port}")
          >> logger.info(s"Connected to postgres @ ${dbConfig.jdbcUrl}, with ${dbConfig.threads} threads.")
          >> IO.never
      }
    }

}
