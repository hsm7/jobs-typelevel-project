package io.github.hsm7.jobs.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

case class EmberConfig (host: Host, port: Port)

object EmberConfig {

  given emberConfigReader: ConfigReader[EmberConfig] =
    ConfigReader.forProduct2("host", "port")(EmberConfig(_, _))(hostReader, portReader)

  private val hostReader: ConfigReader[Host] = ConfigReader[String].emap { host =>
    Host.fromString(host)
      .toRight(CannotConvert(host, Host.getClass.toString, s"Invalid host string: $host"))
  }

  private val portReader: ConfigReader[Port] = ConfigReader[Int].emap { port =>
    Port.fromInt(port)
      .toRight(CannotConvert(port.toString, Host.getClass.toString, s"Invalid host number: $port"))
  }

}
