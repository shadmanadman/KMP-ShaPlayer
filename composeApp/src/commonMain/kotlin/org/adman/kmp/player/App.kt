package org.adman.kmp.player

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun KmpShaPlayer(
    modifier: Modifier? = null,
    urlOrPathToFile: String,
    isLandscape: Boolean = false,
    stop: Boolean = false,
    onLoading: ((isLoading: Boolean) -> Unit) = {},
    onError: ((error: Throwable) -> Unit) = {}
) {
    MediaPlayer(modifier, urlOrPathToFile, isLandscape, stop, onLoading, onError)
}