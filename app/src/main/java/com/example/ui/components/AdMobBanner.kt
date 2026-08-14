package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = ""
) {
    AdMobNativeAdCard(modifier = modifier)
}
