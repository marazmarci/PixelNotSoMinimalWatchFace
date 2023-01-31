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