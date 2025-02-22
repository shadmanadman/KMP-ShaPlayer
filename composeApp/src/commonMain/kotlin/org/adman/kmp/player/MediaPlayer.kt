package org.adman.kmp.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun MediaPlayer(modifier: Modifier?= null,
                                urlOrPathToFile: String,
                                isLandscape: Boolean = false,
                                stop: Boolean = false,
                                onMediaReadyToPlay: (() -> Unit) = {})