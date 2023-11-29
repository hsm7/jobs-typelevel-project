package io.github.hsm7.jobs.modules

import cats.effect.{Async, Resource}
import doobie.{ExecutionContexts, Transactor}
import doobie.hikari.HikariTransactor
import io.github.hsm7.jobs.config.DatabaseConfig

final class Database[F[_]: Async] private (val xa: Transactor[F])
object Database {

  def apply[F[_]: Async](dbConfig: DatabaseConfig): Resource[F, Database[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](dbConfig.threads)
    xa <- HikariTransactor.newHikariTransactor[F](
      dbConfig.dbClassName,
      dbConfig.jdbcUrl,
      dbConfig.username,
      dbConfig.password,
      ec
    )
  } yield new Database[F](xa)

}
