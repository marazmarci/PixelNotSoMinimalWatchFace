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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.main.subviews

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.view.MainViewModel
import me.relex.circleindicator.CircleIndicator

@Composable
fun NotPremium(viewModel: MainViewModel) {
    val context = LocalContext.current

    NotPremiumLayout(
        becomePremiumButtonPressed = {
            viewModel.launchPremiumBuyFlow(context as Activity)
        },
        redeemPromoCodeButtonPressed = viewModel::onRedeemPromoCodeButtonPressed,
        installWatchFaceButtonPressed = viewModel::onGoToInstallWatchFaceButtonPressed,
    )
}

@Composable
private fun NotPremiumLayout(
    becomePremiumButtonPressed: () -> Unit,
    redeemPromoCodeButtonPressed: () -> Unit,
    installWatchFaceButtonPressed: () -> Unit,
    drawViewPager: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Setup the watch face",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "To access the settings, everything happens on your watch: on the watch face, long press on the time. After 1s, you'll see a button appear at the bottom, tap on it to customize.",
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Unlock premium features",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Pager(
            drawViewPager = drawViewPager,
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "To unlock widgets, weather, battery indicators and notification icons, become a premium user:",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = becomePremiumButtonPressed) {
            Text(text = "Become premium")
        }
        Text(
            text = "(1 time payment, no hidden fees)",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(5.dp))

        TextButton(onClick = redeemPromoCodeButtonPressed) {
            Text(text = "Redeem code")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(color = Color.DarkGray, shape = RoundedCornerShape(10))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Watch face not installed on your watch?",
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(5.dp))

            Button(
                onClick = installWatchFaceButtonPressed,
                colors = blueButtonColors(),
            ) {
                Text(text = "Install watch face")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun Pager(drawViewPager: Boolean = true) {
    if (drawViewPager) {
        AndroidView(
            factory = { context ->
                val layout = LayoutInflater.from(context).inflate(R.layout.not_premium_view_pager, null)
                val pager = layout.findViewById<ViewPager>(R.id.not_premium_view_pager)
                val indicator = layout.findViewById<CircleIndicator>(R.id.not_premium_view_pager_indicator)
                pager.adapter = object : FragmentStatePagerAdapter((context as AppCompatActivity).supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

                    override fun getItem(position: Int): Fragment = Fragment(when(position) {
                        0 -> R.layout.fragment_premium_1
                        1 -> R.layout.fragment_premium_2
                        2 -> R.layout.fragment_premium_3
                        else -> throw IllegalStateException("invalid position: $position")
                    })

                    override fun getCount(): Int = 3
                }

                indicator.setViewPager(pager)
                layout
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color.DarkGray),
        )
    }

}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        NotPremiumLayout(
            becomePremiumButtonPressed = {},
            redeemPromoCodeButtonPressed = {},
            installWatchFaceButtonPressed = {},
            drawViewPager = false,
        )
    }
}