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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings.nodesettings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColorCategory
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class PhoneComplicationColorScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    device: Device,
): ViewModel() {
    private val colorCategoriesMutableStateFlow = MutableStateFlow<List<ComplicationColorCategory>>(emptyList())
    val colorCategoriesStateFlow: StateFlow<List<ComplicationColorCategory>> = colorCategoriesMutableStateFlow

    private val eventMutableFlow = MutableLiveFlow<Event>()
    val eventFlow: Flow<Event> = eventMutableFlow

    init {
        colorCategoriesMutableStateFlow.value = listOf(
            ComplicationColorCategory(
                label = "Custom",
                colors = listOfNotNull(
                    ComplicationColor(
                        color = Color.parseColor("#00FFFFFF"),
                        label = CUSTOM_COLOR_LABEL,
                        isDefault = false,
                    ),
                    device.getMaterialUColor()?.let { materialUColorInt ->
                        ComplicationColor(
                            color = materialUColorInt,
                            label = MATERIAL_U_LABEL,
                            isDefault = false,
                        )
                    }
                )
            )
        )
    }

    fun onColorClicked(color: ComplicationColor) {
        viewModelScope.launch {
            when(color.label) {
                CUSTOM_COLOR_LABEL -> eventMutableFlow.emit(Event.StartColorPicker)
                MATERIAL_U_LABEL -> eventMutableFlow.emit(Event.ShowMaterialUColorAlert(color))
                else -> eventMutableFlow.emit(Event.SelectColorEvent(color))
            }
        }
    }

    fun onMaterialUAlertDismissed(color: ComplicationColor) {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.SelectColorEvent(color))
        }
    }

    sealed class Event {
        class SelectColorEvent(val color: ComplicationColor) : Event()
        class ShowMaterialUColorAlert(val color: ComplicationColor) : Event()
        object StartColorPicker : Event()
    }

    companion object {
        private const val CUSTOM_COLOR_LABEL = "Custom color"
        private const val MATERIAL_U_LABEL = "Material You"
    }
}