package com.net.down

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.net.down.ads.AdManager
import com.net.down.ui.NetDownAppRoot
import com.net.down.ui.theme.NetDownTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.attachActivity(this)

        val sharedText = intent
            .takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            ?.let { raw -> Regex("https?://\\S+").find(raw)?.value }

        setContent {
            NetDownTheme {
                NetDownAppRoot(sharedText = sharedText)
            }
        }
    }
}
