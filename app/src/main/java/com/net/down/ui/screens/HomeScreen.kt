package com.net.down.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.net.down.R
import com.net.down.data.model.FormatItem
import com.net.down.data.model.MediaInfo
import com.net.down.ui.components.AppLogo
import com.net.down.ui.components.BannerAd
import com.net.down.ui.components.DurationBadge
import com.net.down.ui.components.FormatOptionRow
import com.net.down.ui.components.GradientButton
import com.net.down.ui.theme.Indigo
import com.net.down.ui.theme.VioletDark
import com.net.down.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    sharedText: String?
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) viewModel.updateUrl(sharedText.trim())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.urlInput,
                    onValueChange = viewModel::updateUrl,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Paste video URL here…") },
                    leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                    trailingIcon = {
                        Row {
                            if (state.urlInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateUrl("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                            IconButton(onClick = {
                                clipboard.getText()?.let { viewModel.updateUrl(it.text.trim()) }
                            }) {
                                Icon(Icons.Filled.ContentPaste, contentDescription = "Paste")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { viewModel.analyze(state.urlInput) }
                    )
                )

                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                GradientButton(
                    text = if (state.isAnalyzing) "Analyzing…" else "Get Video",
                    enabled = !state.isAnalyzing && state.engineReady,
                    loading = state.isAnalyzing,
                    onClick = { viewModel.analyze(state.urlInput) }
                )

                if (!state.engineReady) {
                    Text(
                        text = state.engineError ?: "Preparing download engine (one-time setup)…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        state.mediaInfo?.let { info ->
            MediaResultCard(
                info = info,
                onDownload = { format, isAudio ->
                    viewModel.download(info, format, isAudio)
                },
                onClose = viewModel::dismissResult
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HomeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(VioletDark, Indigo)))
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppLogo(size = 46.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.tagline),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Paste a link and download in HD",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "YouTube, TikTok, Facebook, Instagram, Twitter, Reddit & 1000+ more sites",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MediaResultCard(
    info: MediaInfo,
    onDownload: (FormatItem, Boolean) -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = info.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )
                info.durationLabel?.let { label ->
                    Box(modifier = Modifier.padding(12.dp).align(Alignment.BottomStart)) {
                        DurationBadge(label)
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                info.uploader?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Choose format",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormatOptionRow(
                        resolution = "Best Quality",
                        detail = "MP4  •  best video + audio (auto merge)",
                        isHighlight = true,
                        onClick = {
                            onDownload(
                                FormatItem(
                                    formatId = MediaInfo.FORMAT_BEST,
                                    note = "Best quality",
                                    extension = "mp4",
                                    resolution = "Best Quality",
                                    vcodec = null,
                                    acodec = null,
                                    fileSize = 0L,
                                    hasVideo = true,
                                    hasAudio = true,
                                    fps = null
                                ),
                                false
                            )
                        }
                    )

                    if (info.hasAudioOnlyFormats) {
                        FormatOptionRow(
                            resolution = "MP3 Audio",
                            detail = "MP3  •  extract audio only",
                            isAudio = true,
                            onClick = {
                                onDownload(
                                    FormatItem(
                                        formatId = MediaInfo.FORMAT_AUDIO,
                                        note = "Audio only",
                                        extension = "mp3",
                                        resolution = "MP3 Audio",
                                        vcodec = null,
                                        acodec = null,
                                        fileSize = 0L,
                                        hasVideo = false,
                                        hasAudio = true,
                                        fps = null
                                    ),
                                    true
                                )
                            }
                        )
                    }

                    info.videoFormats.forEach { format ->
                        FormatOptionRow(
                            resolution = format.resolution,
                            detail = format.detailLine,
                            onClick = { onDownload(format, false) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        BannerAd(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
    }
}
