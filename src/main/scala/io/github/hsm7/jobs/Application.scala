package io.github.hsm7.jobs

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import io.github.hsm7.jobs.config.syntax.*
import io.github.hsm7.jobs.config.EmberConfig
import io.github.hsm7.jobs.http.HttpApi

object Application extends IOApp.Simple {

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, EmberConfig].flatMap { emberConfig =>
    EmberServerBuilder
      .default[IO]
      .withHttpApp(HttpApi[IO].routes.orNotFound)
      .withHost(emberConfig.host)
      .withPort(emberConfig.port)
      .build
      .use(_ => IO.never)
  }
}
