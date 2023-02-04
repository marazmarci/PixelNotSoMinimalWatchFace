package com.benoitletondor.pixelminimalwatchface.settings.notificationssync.betadisclaimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.common.compose.productSansFontFamily
import com.benoitletondor.pixelminimalwatchface.compose.WearTheme
import com.benoitletondor.pixelminimalwatchface.compose.component.ChipButton
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn

class BetaDisclaimerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        WearTheme {
            RotatoryAwareLazyColumn(
                horizontalPadding = 20.dp,
            ) {
                Items()
            }
        }
    }

    private fun LazyListScope.Items() {
        item {
            Text(
                text = "Phone notification icons beta disclaimer",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillParentMaxWidth(),
                fontSize = 16.sp,
                fontFamily = productSansFontFamily,
            )
        }

        item {
            Text(
                text = "This feature is in beta, so things might not work perfectly yet.",
                modifier = Modifier.padding(bottom = 6.dp, top = 6.dp),
            )
        }

        item {
            Text(
                text = "There are some known limitations:",
            )
        }

        item {
            Text(
                text = "- It cannot display your watch notifications, only your phone ones, there's no way for a watch face to access them.",
            )
        }

        item {
            Text(
                text = "- Since it's phone notifications, there can be a delta between the notifications you see on your watch and the icons displayed.",
            )
        }

        item {
            Text(
                text = "- It relies on a sync between your phone and watch so a lot of things can go wrong: network issues, WearOS specific bugs etc...",
            )
        }

        item {
            Text(
                text = "Please send me an email if you notice issues or want to provide feedback.",
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
            )
        }

        item {
            ChipButton(
                text = "Understood",
                onClick = {
                    finish()
                }
            )
        }
    }

    @Composable
    @Preview(widthDp = 200, heightDp = 200)
    private fun Preview() {
        LazyColumn {
            Items()
        }
    }
}