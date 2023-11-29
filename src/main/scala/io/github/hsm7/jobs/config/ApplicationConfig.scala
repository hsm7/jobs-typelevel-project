package io.github.hsm7.jobs.config

import pureconfig.ConfigReader

case class ApplicationConfig (databaseConfig: DatabaseConfig, serverConfig: ServerConfig)

object ApplicationConfig {

  given applicationConfigReader: ConfigReader[ApplicationConfig] =
    ConfigReader.forProduct2("database-config", "server-config")(ApplicationConfig(_, _))

}