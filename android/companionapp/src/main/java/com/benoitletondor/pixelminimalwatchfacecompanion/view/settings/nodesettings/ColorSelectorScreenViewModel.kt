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

import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorSelectorScreenViewModel @Inject constructor(
    private val storage: Storage,
) : ViewModel() {
    private val eventMutableFlow = MutableLiveFlow<Event>()
    val eventFlow: Flow<Event> = eventMutableFlow

    val recentColors = storage.getRecentCustomColors()

    fun onColorSelected(@ColorInt colorInt: Int) {
        if (colorInt !in recentColors) {
            storage.setRecentCustomColors(listOf(colorInt) + recentColors.take(4))
        }

        viewModelScope.launch {
            eventMutableFlow.emit(Event.SendResultAndGoBack(colorInt))
        }
    }

    sealed class Event {
        class SendResultAndGoBack(@ColorInt val colorInt: Int) : Event()
    }
}