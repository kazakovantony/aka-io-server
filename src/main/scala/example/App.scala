package example

import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util
import java.util.Collections

import akka.io.Tcp.Connect
import akka.stream.ActorMaterializer

class ClListener extends Actor {

  val connections = Collections.synchronizedList(new util.ArrayList[String]())

  def receive = {
    case x: ByteString => {
      connections.add(x.decodeString(Charset.forName("US-ASCII")))
      println(x)
      println (x.decodeString(Charset.forName("US-ASCII")))}
  }
}

class ClListener2 extends Actor {

  val connections = Collections.synchronizedList(new util.ArrayList[String]())

  def receive = {
    case x: ByteString => {
      connections.add(x.decodeString(Charset.forName("US-ASCII")))
      println(x)
      println (x.decodeString(Charset.forName("US-ASCII")))}
  }
}

class ClClient(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  import context.system

  {
    println("first")
    IO(Tcp) ! Connect(remote)
    println("second")
  }

  def receive = {
    case CommandFailed(_: Connect) ⇒ {
      println("failed1")
      listener ! "failed"
      context stop self
    }

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

class ClClient2(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import Tcp._
  import context.system

  {
    println("first2")
    IO(Tcp) ! Connect(remote)
    println("second3")
  }

  def receive = {
    case CommandFailed(_: Connect) ⇒ {
      println("failed1")
      listener ! "failed"
      context stop self
    }

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


object App {

  def main(args: Array[String]): Unit = {
    //implicit val actorSystem: ActorSystem = ActorSystem("system")
    //implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    //val system = ActorSystem( "hellokernel" )
    //val listener = system.actorOf( Props[ClListener], "Listener" )
    //val remote = new InetSocketAddress( "localhost", 9999)
    //val props = Props( classOf[ClClient], remote, listener )
    //val client = system.actorOf( props, "Client" )

    //val listener2 = system.actorOf( Props[ClListener2], "Listener2" )
    //val remote1 = new InetSocketAddress( "localhost", 9999)

    //val props1 = Props( classOf[ClClient2], remote1, listener2 )
    //val client1 = system.actorOf( props1, "Client1" )

    val remote1 = new InetSocketAddress( "localhost", 9999)
    val r = Connect(remote1)

    Thread.sleep( 200000 )
  }
}
