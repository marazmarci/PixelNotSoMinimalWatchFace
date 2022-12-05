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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.*

@Composable
fun SettingChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String,
    secondaryLabel: String? = null,
    @DrawableRes iconDrawable: Int?,
) {
    Chip(
        modifier = modifier
            .fillMaxWidth(),
        label = { Text(
            text = label,
        ) },
        secondaryLabel = secondaryLabel?.let {
            { SettingButtonSecondaryText(
                text = it,
            ) }
        },
        onClick = onClick,
        icon = iconDrawable?.let {
            { Icon(
                painter = painterResource(id = it),
                contentDescription = null,
            ) }
        },
        colors = ChipDefaults.primaryChipColors(
            backgroundColor = MaterialTheme.colors.surface,
        ),
    )
}