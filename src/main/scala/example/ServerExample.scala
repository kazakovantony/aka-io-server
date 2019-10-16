package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, pathSingleSlash}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

trait ServerExample {
  val smallRoute: Route =
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
    }
}

class Server extends ServerExample

object ServerExample{

  def main(args: Array[String]) {
    implicit val actorSystem: ActorSystem = ActorSystem("system")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    Http().bindAndHandle(new Server().smallRoute,"localhost",8080)

    println("server started at 8080")
  }
}
