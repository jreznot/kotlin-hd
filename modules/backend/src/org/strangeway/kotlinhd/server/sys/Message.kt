package org.strangeway.kotlinhd.server.sys

import com.google.gson.JsonObject

data class Message(val id: String,
                   val method: String,
                   val payload: JsonObject?)