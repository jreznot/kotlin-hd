package org.strangeway.kotlinhd.server

import com.google.gson.Gson
import org.strangeway.kotlinhd.model.Todo
import org.strangeway.kotlinhd.server.service.TodoService
import org.strangeway.kotlinhd.server.sys.Message
import org.strangeway.kotlinhd.server.sys.Response
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import kotlin.system.exitProcess

object Server {
    private val gson = Gson()

    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting backend...")

        val pipe: RandomAccessFile

        try {
            pipe = RandomAccessFile("\\\\.\\pipe\\demo", "rw")
        } catch (f: FileNotFoundException) {
            println("Unable to open communication pipe")

            exitProcess(-7)
        }

        var data: String? = pipe.readLine()
        while (data != null) {
            dataReceived(data, pipe)

            data = pipe.readLine()
        }

        println("Communication pipe closed. Exit")
    }

    private fun dataReceived(jsonMessage: String, pipe: RandomAccessFile) {
        println("Received message: $jsonMessage")

        val message = gson.fromJson(jsonMessage, Message::class.java)

        when (message.method) {
            "hello" -> {
                val response = Response(message.id, gson.toJsonTree("Hi!").asJsonObject)
                pipe.writeBytes(gson.toJson(response) + "\n")
            }
            "list" -> {
                val response = Response(message.id, gson.toJsonTree(TodoService.list()).asJsonObject)
                pipe.writeBytes(gson.toJson(response) + "\n")
            }
            "add" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.add(item)

                val response = Response(message.id, gson.toJsonTree(item).asJsonObject)
                pipe.writeBytes(gson.toJson(response) + "\n")
            }
            "remove" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.remove(item)

                val response = Response(message.id, gson.toJsonTree(item).asJsonObject)
                pipe.writeBytes(gson.toJson(response) + "\n")
            }
            "update" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.update(item)

                val response = Response(message.id, gson.toJsonTree(item).asJsonObject)
                pipe.writeBytes(gson.toJson(response) + "\n")
            }
        }
    }
}