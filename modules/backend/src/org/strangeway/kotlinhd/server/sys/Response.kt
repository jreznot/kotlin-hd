package org.strangeway.kotlinhd.server.sys

import com.google.gson.JsonElement

data class Response(val id: String,
                    val payload: JsonElement?)