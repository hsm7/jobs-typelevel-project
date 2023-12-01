package io.github.hsm7.jobs.http.validation.validators

import cats.data.ValidatedNel
import cats.implicits.*
import io.github.hsm7.jobs.domain.job.JobInfo
import io.github.hsm7.jobs.http.validation.Validator

import java.net.URI
import scala.util.{Failure, Success, Try}

def nonBlank(value: String, fieldName: String): ValidatedNel[(String, String), String] =
  if (value.isBlank) (fieldName -> "Field cannot be empty").invalidNel
  else value.validNel

def validUrl(value: String, fieldName: String): ValidatedNel[(String, String), String] =
  Try(new URI(value).toURL) match
    case Success(_) => value.validNel
    case Failure(_) => (fieldName -> "Invalid URL string").invalidNel

given jobValidator: Validator[JobInfo] = jobInfo =>
  (
    nonBlank(jobInfo.title, "title"),
    nonBlank(jobInfo.company, "company"),
    nonBlank(jobInfo.description, "description"),
    validUrl(jobInfo.externalUrl, "externalUrl"),
    nonBlank(jobInfo.location, "location"),
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
