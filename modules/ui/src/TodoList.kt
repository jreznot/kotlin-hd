import org.strangeway.kotlinhd.model.Todo
import kotlin.browser.document
import kotlin.browser.window

@JsName("jQuery")
external fun jQuery(selector: String): dynamic = definedExternally

external class Pipe {
    fun send(method: String)
}

fun main(args: Array<String>) {
    val datepicker = jQuery("#datepicker")
    datepicker.datepicker()
    datepicker.datepicker("option", "dateFormat", "dd/mm/yy")

    jQuery(".task-container").droppable()
    jQuery(".todo-task").draggable(mapOf(
            "revert" to "valid",
            "revertDuration" to 200
    ))

    // todo import APIs

    // todo start data loading
}

fun createTodoElement() {

}

fun addTodo() {

}

fun removeTodo() {

}

fun showDialog() {

}