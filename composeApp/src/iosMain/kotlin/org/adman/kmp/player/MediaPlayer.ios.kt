package org.adman.kmp.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun MediaPlayer(
    modifier: Modifier?,
    urlOrUri: String,
    isLandscape: Boolean,
    stop: Boolean,
    onLoading: (isLoading:Boolean) -> Unit,
    onError: (error:Throwable) -> Unit
) {

}