package org.strangeway.kotlinhd.server

import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import org.strangeway.kotlinhd.model.Todo
import org.strangeway.kotlinhd.server.service.TodoService
import org.strangeway.kotlinhd.server.sys.IdValue
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
            try {
                dataReceived(data.trim(), pipe)
            } catch (e: RuntimeException) {
                println("Error during request processing: " + e.message)
            }

            data = pipe.readLine()
        }

        println("Communication pipe closed. Exit")
    }

    private fun dataReceived(jsonMessage: String, pipe: RandomAccessFile) {
        println("Received message: $jsonMessage")

        val message = gson.fromJson(jsonMessage, Message::class.java)

        when (message.method) {
            "hello" -> {
                val response = Response(message.id, JsonPrimitive("Hi!"))
                pipeSend(pipe, response)
            }
            "list" -> {
                val response = Response(message.id, gson.toJsonTree(TodoService.list()).asJsonArray)
                pipeSend(pipe, response)
            }
            "get" -> {
                val idValue = gson.fromJson(message.payload, IdValue::class.java)
                val response = Response(message.id, gson.toJsonTree(TodoService.get(idValue.id)).asJsonObject)
                pipeSend(pipe, response)
            }
            "add" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.add(item)

                val response = Response(message.id, gson.toJsonTree(item).asJsonObject)
                pipeSend(pipe, response)
            }
            "remove" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.remove(item)

                val response = Response(message.id, gson.toJsonTree(item).asJsonObject)
                pipeSend(pipe, response)
            }
            "update" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.update(item)

                val response = Response(message.id, gson.toJsonTree(item).asJsonObject)
                pipeSend(pipe, response)
            }
        }
    }

    private fun pipeSend(pipe: RandomAccessFile, response: Any) {
        pipe.writeBytes(gson.toJson(response) + "\n")
    }
}