package io.github.hsm7.jobs.http.validation.validators

import cats.data.ValidatedNel
import cats.implicits.*
import io.github.hsm7.jobs.domain.job.JobInfo
import io.github.hsm7.jobs.http.validation.Validator

def nonEmpty(value: String, fieldName: String): ValidatedNel[String, String] =
  if (value.isBlank) s"Field '$fieldName' cannot be empty".invalidNel
  else value.validNel

given jobValidator: Validator[JobInfo] = jobInfo =>
  (
    nonEmpty(jobInfo.title, "title"),
    nonEmpty(jobInfo.company, "company"),
    nonEmpty(jobInfo.description, "description"),
    nonEmpty(jobInfo.externalUrl, "externalUrl"),
    nonEmpty(jobInfo.location, "location"),
    jobInfo.remote.validNel,
    jobInfo.seniority.validNel,
    jobInfo.salaryLow.validNel,
    jobInfo.salaryHigh.validNel,
    jobInfo.currency.validNel,
    jobInfo.country.validNel,
    jobInfo.tags.validNel,
    jobInfo.image.validNel,
    jobInfo.other.validNel
  ).mapN(JobInfo.apply)
