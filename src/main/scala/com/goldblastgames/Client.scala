package com.goldblastgames

import scala.io.Source

import _root_.reactive.BufferSignal
import _root_.reactive.Observing
import _root_.reactive.EventSource

import com.goldblastgames.reactive.socket.SocketSink
import com.goldblastgames.reactive.socket.SocketSource

object Client extends App with Observing {
  require(
    args.length == 3, 
    "Specify name, sourceport, and sinkport as arguments"
  )
  val name = args(0)
  val sourcePort = args(1).toInt
  val sinkPort = args(2).toInt

  val events = new EventSource[Message]
  val source = SocketSource[Message]("127.0.1.1", sourcePort)
  val sink = SocketSink[Message]("127.0.1.1", sinkPort, events)

  val channelMatcher = """/channel (.+)""".r
  var dest = "all"

  // Take lines from standard input
  new IteratorStream(Source.stdin.getLines.takeWhile(_ != "/quit"))
    .foreach(
      line => line match {
        case channelMatcher(channel) =>
          dest = channel
        case _ => events.fire(new Message(name, dest, line.toString))
      }
    )
  source.foreach(message =>  
    println(message.toString)
  )
}

// Creates an EventSource out of an iterator
// by waiting for the iterator in a separate thread
class IteratorStream[T] (iterator: Iterator[T]) extends EventSource[T] {
  new Thread(new Runnable {
      def run() {
        while(iterator.hasNext) {
          fire(iterator.next)
        }
    }
  }).start
}


