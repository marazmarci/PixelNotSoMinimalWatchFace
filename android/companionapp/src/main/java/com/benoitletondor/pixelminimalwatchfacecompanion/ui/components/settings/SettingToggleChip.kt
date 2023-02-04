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
package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "PlatformSettingToggleChip"

@Composable
fun PlatformSettingToggleChip(
    modifier: Modifier,
    checked: Boolean,
    onCheckedChange: suspend (Boolean) -> Unit,
    label: String,
    secondaryLabel: String?,
    iconDrawable: Int?,
    isInitiallyLoading: Boolean = false,
) {
    val isLoading = remember { mutableStateOf(isInitiallyLoading) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    fun performCheckAction(checked: Boolean) {
        if (!isLoading.value) {
            isLoading.value = true

            scope.launch {
                try {
                    onCheckedChange(checked)
                } catch (e: Exception) {
                    if (e is CancellationException) { throw e }

                    Log.e(TAG, "Error while performing click action", e)
                    Toast.makeText(context, "Unable to apply setting, please try again or reload using the top menu if it persists", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading.value = false
                }
            }
        }
    }

    Chip(
        modifier = modifier,
        onClick = {
            performCheckAction(!checked)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconDrawable != null) {
                Icon(
                    painter = painterResource(id = iconDrawable),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                )

                if (secondaryLabel != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = secondaryLabel,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                contentAlignment = Alignment.Center,
            ) {
                Switch(
                    modifier = Modifier
                        .alpha(if (isLoading.value) { 0f } else { 1f }),
                    checked = checked,
                    onCheckedChange = { checked ->
                        performCheckAction(checked)
                    }
                )

                if (isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(name = "Full preview")
private fun FullPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingToggleChip(
                modifier = Modifier,
                checked = true,
                onCheckedChange = {},
                label = "Test title with a very long text to test what's going on after 1 line",
                secondaryLabel = "Test subtitle with a very long text to test what's going on after 1 line",
                iconDrawable = R.drawable.ic_palette_24,
            )
        }
    }
}

@Composable
@Preview(name = "Full without icon preview")
private fun FullWithoutIconPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingToggleChip(
                modifier = Modifier,
                checked = true,
                onCheckedChange = {},
                label = "Test title with a very long text to test what's going on after 1 line",
                secondaryLabel = "Test subtitle with a very long text to test what's going on after 1 line",
                iconDrawable = null,
            )
        }
    }
}

@Composable
@Preview(name = "Full loading preview")
private fun FullLoadingPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingToggleChip(
                modifier = Modifier,
                checked = true,
                onCheckedChange = {},
                label = "Test title with a very long text to test what's going on after 1 line",
                secondaryLabel = "Test subtitle with a very long text to test what's going on after 1 line",
                iconDrawable = R.drawable.ic_palette_24,
                isInitiallyLoading = true,
            )
        }
    }
}

@Composable
@Preview(name = "Title only preview")
private fun TitleOnlyPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingToggleChip(
                modifier = Modifier,
                checked = true,
                onCheckedChange = {},
                label = "Test title",
                secondaryLabel = null,
                iconDrawable = R.drawable.ic_palette_24,
            )
        }
    }
}

@Composable
@Preview(name = "Title only loading preview")
private fun TitleOnlyLoadingPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingToggleChip(
                modifier = Modifier,
                checked = true,
                onCheckedChange = {},
                label = "Test title",
                secondaryLabel = null,
                iconDrawable = R.drawable.ic_palette_24,
                isInitiallyLoading = true,
            )
        }
    }
}