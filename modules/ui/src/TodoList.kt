import org.strangeway.kotlinhd.api.getServerPipe
import org.strangeway.kotlinhd.api.jQuery
import org.strangeway.kotlinhd.json.TodoSerializer
import org.strangeway.kotlinhd.model.Status
import org.strangeway.kotlinhd.model.Todo
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.dom.createElement
import kotlin.js.json

val pipe = getServerPipe()

fun main(args: Array<String>) {
    jQuery(".task-container").droppable()
    jQuery(".todo-task").draggable(json(
            "revert" to "valid",
            "revertDuration" to 200
    ))

    pipe.send("list", json()).then({ jsonArray ->
        // data loaded - show !
        val todos = TodoSerializer.fromJsonArray(jsonArray)
        for (todo in todos) {
            createTodoElement(todo)
        }
    })

    // Adding drop function to each status of task
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

                        // Updating backend
                        pipe.send("update", TodoSerializer.toJson(todo))
                    }).then({
                        getDeleteDiv().style.display = "none"
                    })
                }
        ))
    }

    // Adding drop function to delete div
    jQuery(getDeleteDiv()).droppable(json(
            "drop" to { _: dynamic, ui: dynamic ->
                val element = ui.helper
                val cssId = element.attr("id").unsafeCast<String>()
                val taskId = cssId.replace("task-", "")

                pipe.send("get", json("id" to taskId)).then({ jsonObject ->
                    val todo = TodoSerializer.fromJson(jsonObject)

                    removeTodoElement(todo)

                    // Updating backend
                    pipe.send("delete", TodoSerializer.toJson(todo))
                }).then({
                    getDeleteDiv().style.display = "none"
                })
            }
    ))

    val taskAddBtn = document.getElementById("task-add-btn")!! as HTMLInputElement
    taskAddBtn.onclick = { addTodo() }
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
    val taskTitleInput = document.getElementById("task-title-input")!! as HTMLInputElement

    if (taskTitleInput.value.isBlank()) {
        showValidationDialog()
        return
    }

    val todo = Todo(null, taskTitleInput.value, Status.BACKLOG)

    taskTitleInput.value = ""

    // Updating backend
    pipe.send("add", TodoSerializer.toJson(todo)).then({ jsonObject ->
        val createdTodo = TodoSerializer.fromJson(jsonObject)
        createTodoElement(createdTodo)
    })
}

fun showValidationDialog() {
    val validationDialog = document.createElement("div", {
        id = "validation-dialog"
        textContent = "Title can not be empty"

        setAttribute("title", "Validation")
    })

    document.body!!.appendChild(validationDialog)

    val jqDialog = jQuery(validationDialog)
    jqDialog.dialog(json(
            "autoOpen" to true,
            "width" to 400,
            "modal" to true,
            "closeOnEscape" to true,
            "buttonsOptions" to json(
                    "Ok" to {
                        jqDialog.dialog("close")
                    }
            )
    ))
}