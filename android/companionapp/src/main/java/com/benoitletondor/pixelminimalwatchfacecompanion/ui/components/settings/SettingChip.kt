package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme

@Composable
fun PlatformSettingChip(
    modifier: Modifier,
    onClick: () -> Unit,
    label: String,
    secondaryLabel: String?,
    iconDrawable: Int?
) {
    Chip(
        modifier = modifier,
        onClick = onClick,
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
        }
    }
}

@Composable
@Preview(name = "Full preview")
private fun FullPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingChip(
                modifier = Modifier,
                label = "Test title with a very long text to test what's going on after 1 line",
                secondaryLabel = "Test subtitle with a very long text to test what's going on after 1 line",
                iconDrawable = R.drawable.ic_palette_24,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview(name = "Full without icon preview")
private fun FullWithoutIconPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingChip(
                modifier = Modifier,
                label = "Test title with a very long text to test what's going on after 1 line",
                secondaryLabel = "Test subtitle with a very long text to test what's going on after 1 line",
                iconDrawable = null,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview(name = "Title only preview")
private fun TitleOnlyPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingChip(
                modifier = Modifier,
                label = "Test title",
                secondaryLabel = null,
                iconDrawable = null,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview(name = "Title only with icon preview")
private fun TitleOnlyWithIconPreview() {
    AppMaterialTheme {
        Column {
            PlatformSettingChip(
                modifier = Modifier,
                label = "Test title",
                secondaryLabel = null,
                iconDrawable = R.drawable.ic_palette_24,
                onClick = {},
            )
        }
    }
}