package ru.sgu.switchmap.utils

import io.circe.Decoder
import org.http4s.client.dsl.Http4sClientDsl
import zio._
import zio.interop.catz._
import org.http4s.ember.client.EmberClientBuilder
import ru.sgu.switchmap.models.{SeenRequest, SeenResponse}
import org.http4s.Uri
import org.http4s.Method.POST
import inet.ipaddr.mac.MACAddress
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._

import scala.concurrent.ExecutionContext.global
import scala.util.matching.Regex
import ru.sgu.switchmap.config.AppConfig
import org.http4s.Request
import org.http4s.EntityDecoder

object seens {
  type SeensUtil = Has[SeensUtil.Service]

  private implicit val zioRuntime: zio.Runtime[zio.ZEnv] =
    zio.Runtime.default

  zioRuntime
    .unsafeRun(
      cats.effect.std
        .Dispatcher[zio.Task]
        .allocated
    )
    ._1

  object SeensUtil {
    import ru.sgu.switchmap.utils.JSONUtil._

    implicit def circeJsonDecoder[A](implicit
      decoder: Decoder[A]
    ): EntityDecoder[Task, A] = jsonOf[Task, A]

    trait Service {
      def get(mac: MACAddress): Task[Option[SeenResponse]]
    }

    val live: ZLayer[Has[AppConfig], Nothing, SeensUtil] =
      ZLayer.fromService { cfg =>
        new Service {
          override def get(mac: MACAddress): Task[Option[SeenResponse]] = {
            val dsl: Http4sClientDsl[Task] = new Http4sClientDsl[Task] {}
            import dsl._

            val uri = Uri.fromString(
              s"${cfg.seensHost}/mac"
            )
            uri match {
              case Right(value) => {
                {
                  val req: Request[Task] = POST(
                    SeenRequest(mac).asJson,
                    value
                  )
                  for {
                    seensAll <- EmberClientBuilder
                      .default[Task]
                      .build
                      .use { client =>
                        client.expect[List[SeenResponse]](req)
                      }
                    seensNow = seensAll.filter(seen =>
                      seen.Name.contains("(now)")
                    )
                    seens = if (seensNow.isEmpty) seensAll else seensNow
                  } yield seens.sortBy(seen => seen.Metric).headOption
                }
              }
              case Left(_) =>
                Task.fail(new Exception("invalid URI for seens"))
            }
          }
        }
      }

    def get(mac: MACAddress): RIO[SeensUtil, Option[SeenResponse]] =
      ZIO.accessM(_.get.get(mac))
  }
}
