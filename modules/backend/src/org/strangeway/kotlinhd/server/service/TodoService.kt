package org.strangeway.kotlinhd.server.service

import org.strangeway.kotlinhd.model.Todo
import java.util.Collections.unmodifiableList

object TodoService {
    private var idSequence = 0

    private val todos = mutableListOf<Todo>()

    fun add(todo: Todo) {
        todo.id = (idSequence++).toString()
        todos.add(todo)
    }

    fun remove(todo: Todo) {
        todos.removeIf { it.id == todo.id }
    }

    fun update(todo: Todo) {
        val existingTodo = todos.find { it.id == todo.id } ?:
                throw IllegalArgumentException("Unable to find todo")

        existingTodo.title = todo.title
    }

    fun list(): List<Todo> {
        return unmodifiableList(todos)
    }
}