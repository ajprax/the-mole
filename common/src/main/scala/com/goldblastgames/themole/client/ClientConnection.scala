package com.goldblastgames.themole.client

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

import com.github.oetzi.echo.core.Event

import com.goldblastgames.themole.io.Connect
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.PacketSerialization._
import com.goldblastgames.themole.server.ClientPlayerInput
import com.goldblastgames.themole.server.PlayerInput
import com.goldblastgames.themole.server.PlayerOutput

object ClientConnection {
  def apply(name: String, host: String, port: Int, messages: Event[Packet]): (ClientPlayerInput, PlayerOutput) = {
    // Build network connection.
    val socket = new Socket(host, port)
    val in = new DataInputStream(socket.getInputStream)
    val out = new DataOutputStream(socket.getOutputStream)

    // Send connection message.
    println("Sending connect message...")
    out.writeUTF(serialize(Connect(name)))
    out.flush()

    // Handle input and output.
    val playerInput = new ClientPlayerInput(name, Event(in))
    val playerOutput = new PlayerOutput(name, Event(out), messages)
    (playerInput, playerOutput)
  }
}

