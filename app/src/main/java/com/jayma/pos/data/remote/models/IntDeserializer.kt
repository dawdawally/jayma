package com.jayma.pos.data.remote.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer that handles empty strings and null values for Int fields.
 * Converts empty strings to 0, null to 0, and valid numbers to Int.
 */
class IntDeserializer : JsonDeserializer<Int> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Int {
        if (json == null || json.isJsonNull) {
            return 0
        }
        
        return when {
            json.isJsonPrimitive -> {
                val primitive = json.asJsonPrimitive
                when {
                    primitive.isString -> {
                        val stringValue = primitive.asString.trim()
                        if (stringValue.isEmpty() || stringValue == "null") {
                            0
                        } else {
                            try {
                                stringValue.toInt()
                            } catch (e: NumberFormatException) {
                                0
                            }
                        }
                    }
                    primitive.isNumber -> primitive.asInt
                    else -> 0
                }
            }
            else -> 0
        }
    }
}
