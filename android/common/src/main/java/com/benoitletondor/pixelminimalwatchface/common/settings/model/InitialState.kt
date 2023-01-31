package com.benoitletondor.pixelminimalwatchface.common.settings.model

import org.json.JSONObject

data class InitialState(
    val isWatchScreenRound: Boolean,
    val isWatchWearOS3: Boolean,
    val watchSupportsWeather: Boolean,
    val hasComplicationsPermission: Boolean,
    val complicationColors: ComplicationColors,
    val settings: Map<String, Any>,
) {
    fun toJson(): String {
        val jsonObject = JSONObject()
        jsonObject.put("isWatchScreenRound", isWatchScreenRound)
        jsonObject.put("isWatchWearOS3", isWatchWearOS3)
        jsonObject.put("watchSupportsWeather", watchSupportsWeather)
        jsonObject.put("hasComplicationsPermission", hasComplicationsPermission)
        jsonObject.put("complicationColors", complicationColors.toJson())
        jsonObject.put("settings", JSONObject().apply {
            settings.map { (key, value) ->
                put(key, value)
            }
        })
        return jsonObject.toString()
    }

    companion object {
        fun fromJson(json: String): InitialState {
            val jsonObject = JSONObject(json)
            val settingsJsonObject = jsonObject.getJSONObject("settings")
            return InitialState(
                isWatchScreenRound = jsonObject.getBoolean("isWatchScreenRound"),
                isWatchWearOS3 = jsonObject.getBoolean("isWatchWearOS3"),
                watchSupportsWeather = jsonObject.getBoolean("watchSupportsWeather"),
                hasComplicationsPermission = jsonObject.getBoolean("hasComplicationsPermission"),
                complicationColors = ComplicationColors.fromJson(jsonObject.getJSONObject("complicationColors")),
                settings = settingsJsonObject
                    .keys()
                    .asSequence()
                    .associateWith { key -> settingsJsonObject.get(key) }
            )
        }
    }
}