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
package com.benoitletondor.pixelminimalwatchface.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.Text
import kotlinx.coroutines.launch

@Composable
fun SettingSlider(
    @DrawableRes iconDrawable: Int,
    onValueChange: suspend (Int) -> Unit,
    value: Int,
    title: String,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = 100,
    step: Int = 25,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(iconDrawable),
                contentDescription = null,
                modifier = Modifier.size(ChipDefaults.SmallIconSize),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(4.dp))

        InlineSlider(
            value = value,
            valueProgression = IntProgression.fromClosedRange(minValue, maxValue, step),
            onValueChange = { coroutineScope.launch { onValueChange(it) } },
            decreaseIcon = { Text(
                text = "-",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            ) },
            increaseIcon = { Text(
                text = "+",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            ) },
            modifier = Modifier.height(30.dp),
        )
    }
}