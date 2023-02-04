/*
 *   Copyright 2022 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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