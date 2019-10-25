package example

import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util
import java.util.Collections

import akka.io.Tcp.Write

import scala.collection.mutable.ListBuffer

//send data for last 10 minutes to client
//send data for whole minute at the end
//parse data to the readable format

class Listener extends Actor {
  def receive = {
    case x: ByteString => {
      println(x)
      println (x.decodeString(Charset.forName("US-ASCII")))}
  }
}

class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) ⇒
      listener ! "failed"
      context stop self

    case c @ Connected(remote, local) ⇒
      listener ! c
      val connection = sender
      connection ! Register(self)
      context become {
        case data: ByteString        ⇒ connection ! Write(data)
        case CommandFailed(w: Write) ⇒ // O/S buffer was full
        case Received(data)          ⇒ listener ! data
        case "close"                 ⇒ connection ! Close
        case _: ConnectionClosed     ⇒ context stop self
      }
  }
}

object ClientTest extends App {

  val system = ActorSystem( "hellokernel" )

  val listener = system.actorOf( Props[Listener], "Listener" )
  val remote = new InetSocketAddress( "localhost", 5555 )

  val props = Props( classOf[Client], remote, listener )
  val client = system.actorOf( props, "Client" )

  val server = system.actorOf( Props[Server], "Server" )

  val handler = system.actorOf( Props[SimplisticHandler], "Handler" )

  Thread.sleep( 20000 )
  server ! Write(ByteString("broadcast"))
  Thread.sleep( 200000 )
}

class SimplisticHandler extends Actor {
  import Tcp._
  def receive = {
    case Received(data) => sender() ! Write(data)
    case PeerClosed     => context.stop(self)
  }
}

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 9999))

  val connections = Collections.synchronizedList(new util.ArrayList[ActorRef]())

  def receive = {

    case b@Bound(localAddress) => {
      context.parent ! b
    }

    case CommandFailed(_: Bind) => context.stop(self)

    case c @Connected(remote, local) =>
      //collect all connection actors here
      //use only nonblocking messaging style for conversation between actors
      println("hop hey -> " + remote)
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)
      connections.add(connection)

      connections.forEach(a => {
        a ! Write(ByteString("Hello"))
        a ! Write(ByteString("Hello2"))
      })

      context become {
        case x: ByteString => {
          println("Server: " + x)
        }
      }
  }
}
