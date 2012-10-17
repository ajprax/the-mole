package com.goldblastgames.themole.client

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

import com.github.oetzi.echo.core.Event

import com.goldblastgames.themole.io.Connect
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.server.PlayerInput
import com.goldblastgames.themole.server.PlayerOutput

object ClientConnection {
  def apply(name: String, host: String, port: Int, messages: Event[Packet]): (PlayerInput, PlayerOutput) = {
    // Build network connection.
    val socket = new Socket(host, port)
    val in = new ObjectInputStream(socket.getInputStream)
    val out = new ObjectOutputStream(socket.getOutputStream)

    // Send connection message.
    println("Sending connect message...")
    out.writeObject(Connect(name))
    out.flush()

    // Handle input and output.
    val playerInput = new PlayerInput(name, Event(in))
    val playerOutput = new PlayerOutput(name, Event(out), messages)
    (playerInput, playerOutput)
  }
}