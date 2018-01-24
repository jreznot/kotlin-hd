package org.strangeway.kotlinhd.json

import org.strangeway.kotlinhd.model.Status
import org.strangeway.kotlinhd.model.Todo
import kotlin.js.Json
import kotlin.js.json

object TodoSerializer {
    fun fromJson(json: Json): Todo {
        return Todo(json["id"] as String, json["title"] as String,
                Status.valueOf(json["status"] as String))
    }

    fun fromJsonArray(json: Json): List<Todo> {
        val array: Array<Json> = json.unsafeCast<Array<Json>>()

        return array.map({ fromJson(it) }).toList()
    }

    fun toJson(todo: Todo): Json {
        return json(
                "id" to todo.id,
                "title" to todo.title,
                "status" to todo.status.name
        )
    }
}