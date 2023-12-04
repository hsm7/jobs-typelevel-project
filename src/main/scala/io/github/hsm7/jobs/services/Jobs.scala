package io.github.hsm7.jobs.services

import cats.implicits.*
import cats.effect.{Sync, Resource}
import doobie.Fragments
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.Read
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import io.github.hsm7.jobs.domain.{ErrorResult, Pagination, ResourceNotFound}
import io.github.hsm7.jobs.domain.job.{Job, JobFilters, JobInfo}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

trait Jobs[F[_]] {

  def getAll(pagination: Pagination, filters: JobFilters): F[List[Job]]
  def getAll: F[List[Job]]
  def get(id: UUID): F[Either[ErrorResult, Job]]
  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID]
  def update(id: UUID, jobInfo: JobInfo): F[Either[ErrorResult, UUID]]
  def delete(id: UUID): F[Unit]
}

class JobService[F[_]: Sync] private(xa: Transactor[F]) extends Jobs[F] {

  given logger: Logger[F] = Slf4jLogger.getLogger[F]

  def getAll(pagination: Pagination, filters: JobFilters): F[List[Job]] = {
    val select: Fragment = fr"""
      |SELECT
      |  id,
      |  date,
      |  ownerEmail,
      |  title,
      |  company,
      |  description,
      |  externalUrl,
      |  location, remote, seniority,
      |  salaryLow,
      |  salaryHigh,
      |  currency,
      |  country,
      |  tags,
      |  image,
      |  other,
      |  active
      |""".stripMargin
    val from: Fragment = fr"FROM jobs"
    val where: Fragment = Fragments.whereAndOpt(
      filters.companies.toNel.map(companies => Fragments.in(fr"company", companies)),
      filters.locations.toNel.map(locations => Fragments.in(fr"location", locations)),
      filters.countries.toNel.map(countries => Fragments.in(fr"country", countries)),
      filters.seniorities.toNel.map(seniorities => Fragments.in(fr"seniority", seniorities)),
      filters.tags.toNel.map { tags =>
        Fragments.or(tags.toList.map(tag => fr"$tag = any(tags)"): _*)
      },
      filters.maxSalary.map(salary => fr"salaryHigh > $salary"),
      filters.remote.map(remote => fr"remote = $remote")
    )
    val paginationFr: Fragment = fr"""
      |ORDER BY date DESC
      |LIMIT ${pagination.limit}
      |OFFSET ${pagination.offset}
      |""".stripMargin

    val statement = select |+| from |+| where |+| paginationFr
    logger.info(statement.toString) >> statement
      .query[Job]
      .to[List]
      .transact(xa)
  }
  def getAll: F[List[Job]] = sql"""
    |SELECT
    |  id,
    |  date,
    |  ownerEmail,
    |  title,
    |  company,
    |  description,
    |  externalUrl,
    |  location, remote, seniority,
    |  salaryLow,
    |  salaryHigh,
    |  currency,
    |  country,
    |  tags,
    |  image,
    |  other,
    |  active
    |FROM jobs
    |""".stripMargin
    .query[Job]
    .to[List]
    .transact(xa)

  def get(id: UUID): F[Either[ErrorResult, Job]] = sql"""
    |SELECT
    |  id,
    |  date,
    |  ownerEmail,
    |  title,
    |  company,
    |  description,
    |  externalUrl,
    |  location,
    |  remote,
    |  seniority,
    |  salaryLow,
    |  salaryHigh,
    |  currency,
    |  country,
    |  tags,
    |  image,
    |  other,
    |  active
    |FROM jobs
    |WHERE id = $id
    |""".stripMargin
    .query[Job]
    .option
    .transact(xa)
    .map {
      case None      => Left(ResourceNotFound(s"No job found with id: $id"))
      case Some(job) => Right(job)
    }

  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID] = sql"""
    |INSERT INTO jobs (
    |  date,
    |  ownerEmail,
    |  title,
    |  company,
    |  description,
    |  externalUrl,
    |  location,
    |  remote,
    |  seniority,
    |  salaryLow,
    |  salaryHigh,
    |  currency,
    |  country,
    |  tags,
    |  image,
    |  other,
    |  active
    |) VALUES (
    |  ${System.currentTimeMillis()},
    |  $ownerEmail,
    |  ${jobInfo.title},
    |  ${jobInfo.company},
    |  ${jobInfo.description},
    |  ${jobInfo.externalUrl},
    |  ${jobInfo.location},
    |  ${jobInfo.remote},
    |  ${jobInfo.seniority},
    |  ${jobInfo.salaryLow},
    |  ${jobInfo.salaryHigh},
    |  ${jobInfo.currency},
    |  ${jobInfo.country},
    |  ${jobInfo.tags},
    |  ${jobInfo.image},
    |  ${jobInfo.other},
    |  true
    |)
    |""".stripMargin.update
    .withUniqueGeneratedKeys[UUID]("id")
    .transact(xa)

  def update(id: UUID, jobInfo: JobInfo): F[Either[ErrorResult, UUID]] = sql"""
    |UPDATE jobs
    |SET
    |  title = ${jobInfo.title},
    |  company = ${jobInfo.company},
    |  description = ${jobInfo.description},
    |  externalUrl = ${jobInfo.externalUrl},
    |  location = ${jobInfo.location},
    |  remote = ${jobInfo.remote},
    |  seniority = ${jobInfo.seniority},
    |  salaryLow = ${jobInfo.salaryLow},
    |  salaryHigh = ${jobInfo.salaryHigh},
    |  currency = ${jobInfo.currency},
    |  country = ${jobInfo.country},
    |  tags = ${jobInfo.tags},
    |  image = ${jobInfo.image},
    |  other = ${jobInfo.other}
    |WHERE id = $id
    |""".stripMargin.update.run
    .transact(xa)
    .map {
      case 0 => Left(ResourceNotFound(s"No job found with id: $id"))
      case _ => Right(id)
    }

  def delete(id: UUID): F[Unit] = sql"""
    |DELETE FROM jobs
    |WHERE id = $id
    |""".stripMargin.update.run
    .transact(xa)
    .void
}

object JobService {
  def apply[F[_]: Sync](xa: Transactor[F]): Resource[F, JobService[F]] =
    Resource.pure(new JobService[F](xa))

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
