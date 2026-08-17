package com.net.down.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.net.down.ads.AdManager

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> AdManager.createBanner(context) },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 60.dp),
        alignment = Alignment.Center
    )
}
