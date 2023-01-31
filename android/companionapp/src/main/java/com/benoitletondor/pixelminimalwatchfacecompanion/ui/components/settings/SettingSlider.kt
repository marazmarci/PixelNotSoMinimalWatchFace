package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "PlatformSettingSlider"

@Composable
fun PlatformSettingSlider(
    iconDrawable: Int,
    onValueChange: suspend (Int) -> Unit,
    value: Int,
    title: String,
    modifier: Modifier,
    minValue: Int,
    maxValue: Int,
    step: Int,
    isInitiallyLoading: Boolean = false,
) {
    val isLoading = remember { mutableStateOf(isInitiallyLoading) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun handleChange(plus: Boolean) {
        if (isLoading.value) { return }
        if (plus && value == maxValue) { return }
        if (!plus && value == minValue) { return }

        scope.launch {
            isLoading.value = true
            try {
                onValueChange(value + if (plus) { step } else { -step })
            } catch (e: Exception) {
                if (e is CancellationException) { throw e }

                Log.e(TAG, "Error while performing click action, + ? $plus", e)
                Toast.makeText(context, "Unable to apply setting, please try again or reload using the top menu if it persists", Toast.LENGTH_LONG).show()
            } finally {
                isLoading.value = false
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 8.dp),
    ) {
        Icon(
            painter = painterResource(id = iconDrawable),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Box(
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .alpha(if (isLoading.value) { 0f } else { 1f })
                ) {
                    Button(
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray,
                        ),
                        onClick = { handleChange(plus = false) },
                    ) {
                        Text(
                            text = "-",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Slider(
                        value = value.toFloat(),
                        onValueChange = {},
                        steps = (maxValue - minValue) / step,
                        valueRange = minValue.toFloat()..maxValue.toFloat(),
                        colors = SliderDefaults.colors(
                            disabledActiveTrackColor = MaterialTheme.colorScheme.onBackground,
                            disabledInactiveTrackColor = MaterialTheme.colorScheme.onBackground,
                            disabledThumbColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = false,
                    )
                    Button(
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray,
                        ),
                        onClick = { handleChange(plus = true) },
                    ) {
                        Text(
                            text = "+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

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

@Preview("With small title")
@Composable
private fun WithSmallTitlePreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingSlider(
                iconDrawable = R.drawable.ic_palette_24,
                onValueChange = {},
                value = 50,
                title = "Test title",
                modifier = Modifier,
                minValue = 0,
                maxValue = 100,
                step = 25,
            )
        }
    }
}

@Preview("With small title")
@Composable
private fun WithLongTitlePreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingSlider(
                iconDrawable = R.drawable.ic_palette_24,
                onValueChange = {},
                value = 50,
                title = "Test title which is very long to see how it behaves with multiple lines of text",
                modifier = Modifier,
                minValue = 0,
                maxValue = 100,
                step = 25,
            )
        }
    }
}
