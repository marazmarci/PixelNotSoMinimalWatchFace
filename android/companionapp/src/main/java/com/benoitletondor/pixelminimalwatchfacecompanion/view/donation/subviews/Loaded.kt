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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.subviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.*
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.DonationViewModel

@Composable
fun Loaded(
    items: List<DonationViewModel.DonationItem>,
    onItemCTAClicked: (DonationViewModel.DonationItem) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "I'm an indie developer, making Pixel Minimal Watch Face on my free time, open source, doing my best to provide a great experience.",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "By being premium you're already supporting a lot but if you feel like donating a bit more, it would really help!",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            items.forEach { item ->
                DonationItemLayout(
                    donationItem = item,
                    onCTAClicked = { onItemCTAClicked(item) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun DonationItemLayout(
    donationItem: DonationViewModel.DonationItem,
    onCTAClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
        Image(
           painter = painterResource(when(donationItem.sku) {
               SKU_DONATION_TIER_1 -> R.drawable.ic_coffee_cup
               SKU_DONATION_TIER_2 -> R.drawable.ic_beer_can
               SKU_DONATION_TIER_3 -> R.drawable.ic_beer
               SKU_DONATION_TIER_4 -> R.drawable.ic_hamburger
               SKU_DONATION_TIER_5 -> R.drawable.ic_burger_beer
               else -> R.drawable.ic_coffee_cup
           }),
           contentDescription = null,
           modifier = Modifier
               .size(50.dp)
               .clip(RoundedCornerShape(size = 16.dp))
               .background(MaterialTheme.colorScheme.secondary)
               .padding(8.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = donationItem.title,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = donationItem.description,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onCTAClicked,
            modifier = Modifier.widthIn(min = 80.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            Text(donationItem.price)
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        Loaded(
            listOf(
                DonationViewModel.DonationItem(
                    sku = SKU_DONATION_TIER_1,
                    title = "Tier 1",
                    description = "Tier 1 description to test the layout with a big long text",
                    price = "2.99e",
                ),
                DonationViewModel.DonationItem(
                    sku = SKU_DONATION_TIER_2,
                    title = "Tier 2",
                    description = "Tier 2 description to test the layout with a big long text",
                    price = "4.99e",
                ),
                DonationViewModel.DonationItem(
                    sku = SKU_DONATION_TIER_3,
                    title = "Tier 3",
                    description = "Tier 3 description to test the layout with a big long text",
                    price = "6.99e",
                ),
                DonationViewModel.DonationItem(
                    sku = SKU_DONATION_TIER_4,
                    title = "Tier 4",
                    description = "Tier 4 description to test the layout with a big long text",
                    price = "8.99e",
                ),
                DonationViewModel.DonationItem(
                    sku = SKU_DONATION_TIER_5,
                    title = "Tier 5",
                    description = "Tier 5 description to test the layout with a big long text",
                    price = "10.99e",
                ),
            ),
            onItemCTAClicked = {},
        )
    }
}