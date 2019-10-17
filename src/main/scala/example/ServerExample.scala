package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.`Set-Cookie`

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ServerExample {

  final case class Item(name: String, id: Long)

  implicit val itemFormat = jsonFormat2(Item)
  val smallRoute: Route =
    concat(
      get {
        concat(
          pathSingleSlash {
            complete {
              "Captain on the bridge!"
            }
          },
          path("ping") {
            complete("PONG!")
          }
        )
      },
      post {
        path("create") {
          entity(as[Item]) { item =>
            print(item)
            complete("order created")
          }
        }
      })
}

class Server extends ServerExample


object ServerExample {

  def main(args: Array[String]) {
    implicit val actorSystem: ActorSystem = ActorSystem("system")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    Http().bindAndHandle(new Server().smallRoute, "localhost", 8080)

    println("server started at 8080")

    val data = ByteString("abc")
    val r = HttpRequest(POST, uri = "http://localhost:8080/create", entity = data)
    val resp : Future[HttpResponse]= Http().singleRequest(r)

    resp.map {
      case response @ HttpResponse(StatusCodes.OK, _, _, _) =>
        val setCookies = response.headers[`Set-Cookie`]
        println(s"Cookies set by a server: $setCookies")
        response.discardEntityBytes()
      case _ => sys.error("something wrong")
    }
  }
}
