package io.github.hsm7.jobs.services

import cats.implicits.*
import cats.effect.MonadCancelThrow
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.Read
import doobie.util.transactor.Transactor

import io.github.hsm7.jobs.domain.job.{Job, JobInfo}

import java.util.UUID

trait Jobs[F[_]] {

  def getAll: F[List[Job]]
  def get(id: UUID): F[Option[Job]]
  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID]
  def update(id: UUID, jobInfo: JobInfo): F[Boolean]
  def delete(id: UUID): F[Boolean]
}

class JobsService[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Jobs[F] {

  def getAll: F[List[Job]] = sql"""
    |SELECT
    |   id,
    |   date,
    |   ownerEmail,
    |   title,
    |   company,
    |   description,
    |   externalUrl,
    |   location, remote, seniority,
    |   salaryLow,
    |   salaryHigh,
    |   currency,
    |   country,
    |   tags,
    |   image,
    |   other,
    |   active
    |FROM jobs
    |""".stripMargin
    .query[Job]
    .to[List]
    .transact(xa)

  def get(id: UUID): F[Option[Job]] = sql"""
    |SELECT
    |   id,
    |   date,
    |   ownerEmail,
    |   title,
    |   company,
    |   description,
    |   externalUrl,
    |   location,
    |   remote,
    |   seniority,
    |   salaryLow,
    |   salaryHigh,
    |   currency,
    |   country,
    |   tags,
    |   image,
    |   other,
    |   active
    |FROM jobs
    |WHERE id = $id
    |""".stripMargin
    .query[Job]
    .option
    .transact(xa)

  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID] = sql"""
    |INSERT INTO jobs (
    |   date,
    |   ownerEmail,
    |   title,
    |   company,
    |   description,
    |   externalUrl,
    |   location,
    |   remote,
    |   seniority,
    |   salaryLow,
    |   salaryHigh,
    |   currency,
    |   country,
    |   tags,
    |   image,
    |   other,
    |   active
    |) VALUES (
    |   ${System.currentTimeMillis()},
    |   $ownerEmail,
    |   ${jobInfo.title},
    |   ${jobInfo.company},
    |   ${jobInfo.description},
    |   ${jobInfo.externalUrl},
    |   ${jobInfo.location},
    |   ${jobInfo.remote},
    |   ${jobInfo.seniority},
    |   ${jobInfo.salaryLow},
    |   ${jobInfo.salaryHigh},
    |   ${jobInfo.currency},
    |   ${jobInfo.country},
    |   ${jobInfo.tags},
    |   ${jobInfo.image},
    |   ${jobInfo.other},
    |   true
    |)
    |""".stripMargin.update
    .withUniqueGeneratedKeys[UUID]("id")
    .transact(xa)

  def update(id: UUID, jobInfo: JobInfo): F[Boolean] = sql"""
    |UPDATE jobs
    |SET
    |   title = ${jobInfo.title},
    |   company = ${jobInfo.company},
    |   description = ${jobInfo.description},
    |   externalUrl = ${jobInfo.externalUrl},
    |   location = ${jobInfo.location},
    |   remote = ${jobInfo.remote},
    |   seniority = ${jobInfo.seniority},
    |   salaryLow = ${jobInfo.salaryLow},
    |   salaryHigh = ${jobInfo.salaryHigh},
    |   currency = ${jobInfo.currency},
    |   country = ${jobInfo.country},
    |   tags = ${jobInfo.tags},
    |   image = ${jobInfo.image},
    |   other = ${jobInfo.other}
    |WHERE id = $id
    |""".stripMargin.update.run
    .transact(xa)
    .map {
      case 0 => false
      case _ => true
    }

  def delete(id: UUID): F[Boolean] = sql"""
    |DELETE FROM jobs
    |WHERE id = $id
    |""".stripMargin.update.run
    .transact(xa)
    .map {
      case 0 => false
      case _ => true
    }

}

object JobsService {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[JobsService[F]] = new JobsService[F](xa).pure[F]

  given jobReader(using
      read: Read[
        (
            UUID,
            Long,
            String,
            String,
            String,
            String,
            String,
            String,
            Boolean,
            Option[String],
            Option[Int],
            Option[Int],
            Option[String],
            Option[String],
            Option[List[String]],
            Option[String],
            Option[String],
            Boolean
        )
      ]
  ): Read[Job] = read.map {
    case (
          id: UUID,
          date: Long,
          ownerEmail: String,
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
          other: Option[String],
          active: Boolean
        ) =>
      Job(
        id = id,
        date = date,
        ownerEmail = ownerEmail,
        JobInfo(
          title = title,
          company = company,
          description = description,
          externalUrl = externalUrl,
          location = location,
          remote = remote,
          seniority = seniority,
          salaryLow = salaryLow,
          salaryHigh = salaryHigh,
          currency = currency,
          country = country,
          tags = tags,
          image = image,
          other = other
        ),
        active = active
      )
  }
}
