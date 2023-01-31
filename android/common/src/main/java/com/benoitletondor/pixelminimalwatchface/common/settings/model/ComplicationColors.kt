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

data class ComplicationColors(
    val leftColor: ComplicationColor,
    val middleColor: ComplicationColor,
    val rightColor: ComplicationColor,
    val bottomColor: ComplicationColor,
    val android12TopLeftColor: ComplicationColor,
    val android12TopRightColor: ComplicationColor,
    val android12BottomLeftColor: ComplicationColor,
    val android12BottomRightColor: ComplicationColor,
    val leftSecondaryColor: ComplicationColor,
    val middleSecondaryColor: ComplicationColor,
    val rightSecondaryColor: ComplicationColor,
    val bottomSecondaryColor: ComplicationColor,
    val android12TopLeftSecondaryColor: ComplicationColor,
    val android12TopRightSecondaryColor: ComplicationColor,
    val android12BottomLeftSecondaryColor: ComplicationColor,
    val android12BottomRightSecondaryColor: ComplicationColor,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("leftColor", leftColor.toJson())
        put("middleColor", middleColor.toJson())
        put("rightColor", rightColor.toJson())
        put("bottomColor", bottomColor.toJson())
        put("android12TopLeftColor", android12TopLeftColor.toJson())
        put("android12TopRightColor", android12TopRightColor.toJson())
        put("android12BottomLeftColor", android12BottomLeftColor.toJson())
        put("android12BottomRightColor", android12BottomRightColor.toJson())
        put("leftSecondaryColor", leftSecondaryColor.toJson())
        put("middleSecondaryColor", middleSecondaryColor.toJson())
        put("rightSecondaryColor", rightSecondaryColor.toJson())
        put("bottomSecondaryColor", bottomSecondaryColor.toJson())
        put("android12TopLeftSecondaryColor", android12TopLeftSecondaryColor.toJson())
        put("android12TopRightSecondaryColor", android12TopRightSecondaryColor.toJson())
        put("android12BottomLeftSecondaryColor", android12BottomLeftSecondaryColor.toJson())
        put("android12BottomRightSecondaryColor", android12BottomRightSecondaryColor.toJson())
    }

    fun toJsonString(): String = toJson().toString()

    private fun ComplicationColor.toJson(): JSONObject = JSONObject().apply {
        put("color", color)
        put("label", label)
        put("isDefault", isDefault)
    }

    companion object {
        fun fromJson(jsonString: String) : ComplicationColors = fromJson(JSONObject(jsonString))

        fun fromJson(jsonObject: JSONObject): ComplicationColors = ComplicationColors(
            leftColor = jsonObject.getJSONObject("leftColor").getComplicationColor(),
            middleColor = jsonObject.getJSONObject("middleColor").getComplicationColor(),
            rightColor = jsonObject.getJSONObject("rightColor").getComplicationColor(),
            bottomColor = jsonObject.getJSONObject("bottomColor").getComplicationColor(),
            android12TopLeftColor = jsonObject.getJSONObject("android12TopLeftColor").getComplicationColor(),
            android12TopRightColor = jsonObject.getJSONObject("android12TopRightColor").getComplicationColor(),
            android12BottomLeftColor = jsonObject.getJSONObject("android12BottomLeftColor").getComplicationColor(),
            android12BottomRightColor = jsonObject.getJSONObject("android12BottomRightColor").getComplicationColor(),
            leftSecondaryColor = jsonObject.getJSONObject("leftSecondaryColor").getComplicationColor(),
            middleSecondaryColor = jsonObject.getJSONObject("middleSecondaryColor").getComplicationColor(),
            rightSecondaryColor = jsonObject.getJSONObject("rightSecondaryColor").getComplicationColor(),
            bottomSecondaryColor = jsonObject.getJSONObject("bottomSecondaryColor").getComplicationColor(),
            android12TopLeftSecondaryColor = jsonObject.getJSONObject("android12TopLeftSecondaryColor").getComplicationColor(),
            android12TopRightSecondaryColor = jsonObject.getJSONObject("android12TopRightSecondaryColor").getComplicationColor(),
            android12BottomLeftSecondaryColor = jsonObject.getJSONObject("android12BottomLeftSecondaryColor").getComplicationColor(),
            android12BottomRightSecondaryColor = jsonObject.getJSONObject("android12BottomRightSecondaryColor").getComplicationColor(),
        )

        private fun JSONObject.getComplicationColor(): ComplicationColor = ComplicationColor(
            color = getInt("color"),
            label = getString("label"),
            isDefault = getBoolean("isDefault"),
        )
    }
}

data class ComplicationColorCategory(
    val label: String,
    val colors: List<ComplicationColor>,
)