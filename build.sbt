ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val org           = "io.github.hsm7"
lazy val scala3Version = "3.3.1"

lazy val circeVersion               = "0.14.1"
lazy val catsEffectVersion          = "3.5.0"
lazy val http4sVersion              = "0.23.19"
lazy val doobieVersion              = "1.0.0-RC1"
lazy val pureConfigVersion          = "0.17.2"
lazy val log4catsVersion            = "2.5.0"
lazy val tsecVersion                = "0.4.0"
lazy val scalaTestVersion           = "3.2.15"
lazy val scalaTestCatsEffectVersion = "1.4.0"
lazy val testContainerVersion       = "1.17.6"
lazy val slf4jVersion               = "2.0.5"
lazy val javaMailVersion            = "1.6.2"

lazy val server = (project in file("."))
  .settings(
    name                := "jobs-typelevel-project",
    scalaVersion        := scala3Version,
    organization        := org,
    Compile / mainClass := Some("io.github.hsm7.jobs.Application"),
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"                   % catsEffectVersion,
      "org.http4s"            %% "http4s-dsl"                    % http4sVersion,
      "org.http4s"            %% "http4s-ember-server"           % http4sVersion,
      "org.http4s"            %% "http4s-circe"                  % http4sVersion,
      "io.circe"              %% "circe-generic"                 % circeVersion,
      "io.circe"              %% "circe-fs2"                     % circeVersion,
      "org.tpolecat"          %% "doobie-core"                   % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"                 % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"               % doobieVersion,
      "org.tpolecat"          %% "doobie-scalatest"              % doobieVersion              % Test,
      "com.github.pureconfig" %% "pureconfig-core"               % pureConfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"                % log4catsVersion,
      "org.slf4j"              % "slf4j-simple"                  % slf4jVersion,
      "io.github.jmcardon"    %% "tsec-http4s"                   % tsecVersion,
      "com.sun.mail"           % "javax.mail"                    % javaMailVersion,
      "org.typelevel"         %% "log4cats-noop"                 % log4catsVersion            % Test,
      "org.scalatest"         %% "scalatest"                     % scalaTestVersion           % Test,
      "org.typelevel"         %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
      "org.testcontainers"     % "testcontainers"                % testContainerVersion       % Test,
      "org.testcontainers"     % "postgresql"                    % testContainerVersion       % Test,
    )
  )
