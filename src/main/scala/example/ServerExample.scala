package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

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
    Http().bindAndHandle(new Server().smallRoute, "localhost", 8080)

    println("server started at 8080")
  }
}
