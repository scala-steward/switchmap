package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.{FloorRequest, FloorResponse}
import ru.sgu.switchmap.repositories._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._

final case class FloorRoutes[R <: Has[Authorizer] with FloorRepository]() {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AppTask, A] =
    jsonEncoderOf[AppTask, A]

  val getFloorsOfEndpoint = withAuth.get
    .in("builds" / path[String]("shortName") / "floors")
    .out(jsonBody[List[FloorResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getFloorsOf(shortName).mapError(_.toString())
        case _ => ZIO.fail("401")
      }
    }
  val getFloorEndpoint = withAuth.get
    .in("builds" / path[String]("shortName") / "floors" / path[Int]("number"))
    .out(jsonBody[FloorResponse])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            getFloor(shortName, number).mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }
  val addFloorEndpoint = withAuth.post
    .in("builds" / path[String]("shortName"))
    .in(jsonBody[FloorRequest])
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, floor) =>
        as match {
          case AuthStatus.Succeed => createFloor(floor).mapError(_.toString())
          case _                  => ZIO.fail("401")
        }
      }
    }
  val deleteFloorEndpoint = withAuth.delete
    .in("builds" / path[String]("shortName") / "floors" / path[Int]("number"))
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            (getFloor(shortName, number) *> deleteFloor(shortName, number))
              .mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }

  val routes = ZHttp4sServerInterpreter()
    .from(
      List(
        getFloorsOfEndpoint,
        getFloorEndpoint,
        addFloorEndpoint,
        deleteFloorEndpoint
      )
    )
    .toRoutes

  val api: RhoRoutes[AppTask] = new RhoRoutes[AppTask] {
    val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
    import swaggerIO._

    "Get all floors of build" **
      GET / "builds" / pv"shortName" / "floors" >>> AuthContext.auth |>> {
        (shortName: String, auth: AuthStatus.Status) =>
          auth match {
            case AuthStatus.Succeed =>
              getFloorsOf(shortName).foldM(_ => NotFound(()), Ok(_))
            case _ => Unauthorized(())
          }
      }

    "Get floor of build by number" **
      GET / "builds" / pv"shortName" / "floors" / pathVar[Int](
        "number",
        "Number of floor"
      ) >>> AuthContext.auth |>> {
        (shortName: String, number: Int, auth: AuthStatus.Status) =>
          auth match {
            case AuthStatus.Succeed =>
              getFloor(shortName, number).foldM(_ => NotFound(()), Ok(_))
            case _ => Unauthorized(())
          }
      }

    "Add floor to build" **
      POST / "builds" / pv"shortName" >>> AuthContext.auth ^ jsonOf[
        AppTask,
        FloorRequest
      ] |>> { (_: String, auth: AuthStatus.Status, floor: FloorRequest) =>
        auth match {
          case AuthStatus.Succeed =>
            createFloor(floor).foldM(
              e => InternalServerError(e.getMessage),
              Created(_)
            )
          case _ => Unauthorized(())
        }
      }

    "Delete floor of build by number" **
      DELETE / "builds" / pv"shortName" / "floors" / pathVar[Int](
        "number",
        "Number of floor"
      ) >>> AuthContext.auth |>> {
        (shortName: String, number: Int, auth: AuthStatus.Status) =>
          auth match {
            case AuthStatus.Succeed =>
              (getFloor(shortName, number) *> deleteFloor(shortName, number))
                .foldM(_ => NotFound(()), Ok(_))
            case _ => Unauthorized(())
          }
      }
  }
}
