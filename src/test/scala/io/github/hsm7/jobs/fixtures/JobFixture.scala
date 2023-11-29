package io.github.hsm7.jobs.fixtures

import java.util.UUID

import io.github.hsm7.jobs.domain.job.{Job, JobInfo}

trait JobFixture {

  val NOT_FOUND_JOB_UUID: UUID = UUID.fromString("6ea79557-3112-4c84-a8f5-1d1e2c300948")
  val SCALA_JOB_UUID: UUID     = UUID.fromString("843df718-ec6e-4d49-9289-f799c0f40064")
  val SCALA_JOB: Job = Job(
    SCALA_JOB_UUID,
    1659186086L,
    "email@example.com",
    JobInfo(
      "Example Company",
      "Senior Scala",
      "A long description of Scala job",
      "https://example.com/applications",
      "Sudan",
      false,
      Some("Senior"),
      Some(2000),
      Some(4000),
      Some("USD"),
      Some("KRT"),
      Some(List("scala", "scala-3", "cats")),
      None,
      None
    ),
    false
  )

  val INVALID_JOB: Job = Job(
    null,
    42L,
    "nothing@gmail.com",
    JobInfo.empty,
    false
  )

  val SCALA_JOB_UPDATED: Job = Job(
    SCALA_JOB_UUID,
    1659186086L,
    "email@example.com",
    JobInfo(
      "Example Company",
      "Fullstack Typelevel Purist",
      "A long description of Scala job",
      "https://example.com/applications",
      "Sudan",
      false,
      Some("Senior"),
      Some(4000),
      Some(8000),
      Some("SDG"),
      Some("KRT"),
      Some(List("scala", "scala-3", "cats", "http4s")),
      None,
      None
    ),
    false
  )

  val ROCK_THE_JVM_JOB: JobInfo = JobInfo(
    "RockTheJvm",
    "Technical Author",
    "For the glory of the RockTheJvm!",
    "https://rockthejvm.com/",
    "From remote",
    true,
    Some("Junior"),
    Some(2000),
    Some(3500),
    Some("EUR"),
    Some("Romania"),
    Some(List("scala", "scala-3", "cats", "akka", "spark", "flink", "zio")),
    None,
    None
  )

  val SCALA_JOB_WITH_NOT_FOUND_UUID: Job = SCALA_JOB.copy(id = NOT_FOUND_JOB_UUID)
  val ANOTHER_SCALA_JOB_UUID: UUID       = UUID.fromString("19a941d0-aa19-477b-9ab0-a7033ae65c2b")
  val ANOTHER_SCALA_JOB: Job             = SCALA_JOB.copy(id = ANOTHER_SCALA_JOB_UUID)
  val ROCK_THE_JVM_SCALA_JOB: Job        = SCALA_JOB.copy(jobInfo = SCALA_JOB.jobInfo.copy(company = "RockTheJvm"))
  val NEW_JOB_UUID: UUID                 = UUID.fromString("efcd2a64-4463-453a-ada8-b1bae1db4377")
  val AWESOME_NEW_JOB: JobInfo = JobInfo(
    "Awesome Company",
    "Tech Lead",
    "An awesome job in Berlin",
    "example.com",
    "Berlin",
    false,
    Some("High"),
    Some(2000),
    Some(3000),
    Some("EUR"),
    Some("Germany"),
    Some(List("scala", "scala-3", "cats")),
    None,
    None
  )
}
