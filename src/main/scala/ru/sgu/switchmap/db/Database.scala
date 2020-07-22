package ru.sgu.switchmap.db

import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import ru.sgu.switchmap.config.Config.DatabaseConfig

import scala.concurrent.ExecutionContext

object Database {

  def transactor(
    config: DatabaseConfig,
    executionContext: ExecutionContext,
    blocker: Blocker
  )(implicit
    contextShift: ContextShift[IO]
  ): Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext,
      blocker
    )
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
  }

}
