package example

import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString

class ServerExampleTest extends WordSpec with Matchers with ScalatestRouteTest with ServerExample {

  "The service" should {

    "return a greeting for GET requests to the root path" in {
      // tests:
      Get() ~> smallRoute ~> check {
        responseAs[String] shouldEqual "Captain on the bridge!"
      }
    }

    "return a 'PONG!' response for GET requests to /ping" in {
      // tests:
      Get("/ping") ~> smallRoute ~> check {
        responseAs[String] shouldEqual "PONG!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      // tests:
      Get("/kermit") ~> smallRoute ~> check {
        handled shouldBe false
      }
    }

    "some POST method test" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "id":1,
           |    "name":"test"
           |}
        """.stripMargin)

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/create",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> smallRoute ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
