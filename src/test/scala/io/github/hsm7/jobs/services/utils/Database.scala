package io.github.hsm7.jobs.services.utils

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import org.testcontainers.containers.PostgreSQLContainer

object Database {

  def makePostgresContainer[F[_]: Async](initScriptPath: String): Resource[F, PostgreSQLContainer[Nothing]] = {
    val acquire: F[PostgreSQLContainer[Nothing]] = Async[F].delay {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres").withInitScript(initScriptPath)
      container.start()
      container
    }
    val release: PostgreSQLContainer[Nothing] => F[Unit] = container => Async[F].delay(container.stop())
    Resource.make(acquire)(release)
  }

  def makeTransactor[F[_]: Async](postgresContainer: PostgreSQLContainer[Nothing]): Resource[F, Transactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](4)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        postgresContainer.getJdbcUrl,
        postgresContainer.getUsername,
        postgresContainer.getPassword,
        ec
      )
    } yield xa
}
