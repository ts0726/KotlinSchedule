package kmp.project.schedule.util.tokenUtil

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun getUsernameFromToken(token: String): String? {
    return try {
        val parts = token.split(".")
        val payload = Base64Url.decode(parts[1]).decodeToString()
        val json = Json.parseToJsonElement(payload) as? JsonObject
        json?.get("name")?.jsonPrimitive?.content
    } catch (e: Exception) {
        null
    }
}

object Base64Url {
    @OptIn(ExperimentalEncodingApi::class)
    fun decode(input: String): ByteArray {
        var base64 = input.replace("-", "+").replace("_", "/")
        when (base64.length % 4) {
            2 -> base64 += "=="
            3 -> base64 += "="
        }
        return Base64.decode(base64)
    }
}