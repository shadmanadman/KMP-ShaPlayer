package org.adman.kmp.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun MediaPlayer(
    modifier: Modifier?,
    urlOrPathToFile: String,
    isLandscape: Boolean,
    stop: Boolean,
    onMediaReadyToPlay: () -> Unit
) {

}