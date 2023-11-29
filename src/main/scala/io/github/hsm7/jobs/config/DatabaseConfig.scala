package io.github.hsm7.jobs.config

import pureconfig.ConfigReader

case class DatabaseConfig(threads: Int, dbClassName: String, jdbcUrl: String, username: String, password: String)

object DatabaseConfig {
  given databaseConfigReader: ConfigReader[DatabaseConfig] =
    ConfigReader
      .forProduct5("threads", "db-class-name", "jdbc-url", "username", "password")(DatabaseConfig(_, _, _, _, _))

}
