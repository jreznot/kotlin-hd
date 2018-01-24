package org.strangeway.kotlinhd.api

import kotlin.js.Json
import kotlin.js.Promise

@JsName("jQuery")
external fun jQuery(selector: Any): dynamic = definedExternally

@JsName("require")
external fun jsRequire(module: String): dynamic = definedExternally

external class Pipe {
    fun send(method: String, payload: Json) : Promise<Json>
}

external class Remote {
    fun getGlobal(name: String): dynamic = definedExternally
}

fun getServerPipe() : Pipe {
    val remote = jsRequire("electron").remote.unsafeCast<Remote>()
    return remote.getGlobal("pipe").unsafeCast<Pipe>()
}