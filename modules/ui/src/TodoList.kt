import kotlinx.html.HTML
import org.strangeway.kotlinhd.api.getServerPipe
import org.strangeway.kotlinhd.api.jQuery
import org.strangeway.kotlinhd.json.TodoSerializer
import org.strangeway.kotlinhd.model.Status
import org.strangeway.kotlinhd.model.Todo
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.dom.createElement
import kotlin.js.json

fun main(args: Array<String>) {
    jQuery(".task-container").droppable()
    jQuery(".todo-task").draggable(json(
            "revert" to "valid",
            "revertDuration" to 200
    ))

    val pipe = getServerPipe()
    pipe.send("list", json()).then({ jsonArray ->
        // data loaded - show !
        val todos = TodoSerializer.fromJsonArray(jsonArray)
        for (todo in todos) {
            createTodoElement(todo)
        }
    })

    for (status in Status.values()) {
        jQuery("#${status.name}").droppable(json(
                "drop" to { _: dynamic, ui: dynamic ->
                    val element = ui.helper
                    val cssId = element.attr("id").unsafeCast<String>()
                    val taskId = cssId.replace("task-", "")

                    pipe.send("get", json("id" to taskId)).then({ jsonObject ->
                        val todo = TodoSerializer.fromJson(jsonObject)

                        removeTodoElement(todo)

                        todo.status = status

                        createTodoElement(todo)

                        pipe.send("update", TodoSerializer.toJson(todo))
                    }).then({
                        getDeleteDiv().style.display = "none"
                    })
                }
        ))
    }

    // todo Adding drop function to delete div

    // todo event handling

    // todo server communication

    // todo check page refresh

    // todo remove todo.js

    // todo comments !
}

fun getDeleteDiv(): HTMLElement {
    return document.getElementById("delete-div")!! as HTMLElement
}

fun createTodoElement(todo: Todo) {
    val todoElement = document.createElement("div") {
        className = "todo-task"
        id = "task-${todo.id}"
        setAttribute("data", todo.id.toString())

        appendChild(document.createElement("div", {
            className = "todo-header"
            textContent = todo.title
        }))
    }
    document.getElementById(todo.status.name)!!
            .appendChild(todoElement)

    jQuery(todoElement).draggable(json(
            "start" to {
                getDeleteDiv().style.display = "block"
            },
            "stop" to {
                getDeleteDiv().style.display = "none"
            },
            "revert" to "invalid",
            "revertDuration" to 200
    ))
}

fun removeTodoElement(todo: Todo) {
    document.getElementById("task-${todo.id}")!!.remove()
}

fun addTodo() {

}

fun removeTodo() {

}

fun showDialog() {

}