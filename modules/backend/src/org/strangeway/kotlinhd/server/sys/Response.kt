package org.strangeway.kotlinhd.server.sys

import com.google.gson.JsonObject

data class Response(val id: String,
                    val payload: JsonObject?)