package ru.sgu.switchmap

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    SwitchmapServer.create()
}
