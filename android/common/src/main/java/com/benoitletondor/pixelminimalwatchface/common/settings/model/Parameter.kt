package com.benoitletondor.pixelminimalwatchface.common.settings.model

import org.json.JSONObject

data class Parameter(
    val key: String,
    val value: Any,
) {
    fun toJson(): String = JSONObject().apply {
        put("key", key)
        put("value", value)
    }.toString()

    companion object {
        fun fromJson(json: String): Parameter {
            val jsonObject = JSONObject(json)
            return Parameter(
                key = jsonObject.getString("key"),
                value = jsonObject.get("value"),
            )
        }
    }

}