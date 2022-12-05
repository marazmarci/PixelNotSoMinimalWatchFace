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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.productSansFontFamily
import kotlinx.coroutines.launch

@Composable
fun OnboardingView(
    navController: NavController,
    viewModel: OnboardingViewModel,
) {
    LaunchedEffect("nav") {
        viewModel.finishEventFlow.collect {
            navController.navigateUp()
        }
    }

    OnboardingViewLayout(
        onContinueButtonPressed = viewModel::onOnboardingFinishButtonPressed,
    )
}

@Composable
private fun OnboardingViewLayout(
    onContinueButtonPressed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 20.dp, horizontal = 26.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to Pixel Minimal Watch Face companion app",
            fontFamily = productSansFontFamily,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(R.drawable.onboarding),
            contentDescription = null,
            modifier = Modifier.height(250.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "This companion phone app will help you install the watch face on your watch and become premium to unlock widgets and weather.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "If you have any issue, please contact me using the app menu for support.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onContinueButtonPressed,
        ) {
            Text(text = "Let's go")
        }
    }
}

@Composable
@Preview
private fun Preview() {
    AppMaterialTheme {
        OnboardingViewLayout(
            onContinueButtonPressed = {},
        )
    }
}