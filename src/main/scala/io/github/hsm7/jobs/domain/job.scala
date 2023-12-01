package io.github.hsm7.jobs.domain

import java.util.UUID

object job {

  case class Job(id: UUID, date: Long, ownerEmail: String, jobInfo: JobInfo, active: Boolean)
  case class JobFilters(companies: Option[List[String]], tags: Option[List[String]], remote: Option[Boolean])

  case class JobInfo(
      title: String,
      company: String,
      description: String,
      externalUrl: String,
      location: String,
      remote: Boolean,
      seniority: Option[String],
      salaryLow: Option[Int],
      salaryHigh: Option[Int],
      currency: Option[String],
      country: Option[String],
      tags: Option[List[String]],
      image: Option[String],
      other: Option[String]
  )

  object JobInfo {
    def apply(
        title: String,
        company: String,
        description: String,
        externalUrl: String,
        location: String,
        remote: Boolean
    ): JobInfo = JobInfo(
      title,
      company,
      description,
      externalUrl,
      location,
      remote,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None
    )
    val empty: JobInfo = JobInfo("", "", "", "", "", false, None, None, None, None, None, None, None, None)

  }

}
