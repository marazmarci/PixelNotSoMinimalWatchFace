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
package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.productSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarScaffold(
    navController: NavController,
    showBackButton: Boolean,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (BoxScope) -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = title,
                        fontFamily = productSansFontFamily,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                },
                actions = actions,
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Up button")
                        }
                    }
                },
            )
        },
        content = {
            Box(modifier = Modifier.padding(it), content = content)
        },
    )
}