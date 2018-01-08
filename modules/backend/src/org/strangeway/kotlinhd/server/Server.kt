package org.strangeway.kotlinhd.server

import com.google.gson.Gson
import org.strangeway.kotlinhd.model.Todo
import org.strangeway.kotlinhd.server.service.TodoService
import org.strangeway.kotlinhd.server.sys.Message
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import kotlin.system.exitProcess

object Server {
    private val gson = Gson()
    private var idSequence = 0

    @JvmStatic
    fun main(args: Array<String>) {
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
        val message = gson.fromJson(jsonMessage, Message::class.java)

        when (message.id) {
            "list" -> {
                val response = gson.toJson(TodoService.list())
                pipe.writeBytes(response + "\n")
            }
            "add" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                item.id = (idSequence++).toString()
                TodoService.add(item)

                val response = gson.toJson(item)
                pipe.writeBytes(response + "\n")
            }
            "remove" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.remove(item)

                val response = gson.toJson(item)
                pipe.writeBytes(response + "\n")
            }
            "update" -> {
                val item = gson.fromJson(message.payload, Todo::class.java)
                TodoService.update(item)

                val response = gson.toJson(item)
                pipe.writeBytes(response + "\n")
            }
        }
    }
}