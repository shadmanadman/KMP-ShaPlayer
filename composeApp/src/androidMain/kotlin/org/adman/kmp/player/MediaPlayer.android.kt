package org.adman.kmp.player

import android.content.pm.ActivityInfo
import android.media.MediaPlayer.MEDIA_ERROR_IO
import android.media.MediaPlayer.MEDIA_ERROR_MALFORMED
import android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED
import android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT
import android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN
import android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal actual fun MediaPlayer(
    modifier: Modifier?,
    urlOrUri: String,
    isLandscape: Boolean,
    stop: Boolean,
    onLoading: (isLoading: Boolean) -> Unit,
    onError: (error: Throwable) -> Unit
) {
    val activity = LocalContext.current as? android.app.Activity

    LaunchedEffect(isLandscape) {
        if (isLandscape)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    onLoading(true)
    AndroidView(
        modifier = modifier ?: Modifier.fillMaxWidth(),
        factory = { context ->
            VideoView(context).apply {
                setVideoPath(urlOrUri)
                val mediaController = MediaController(context)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
                setOnPreparedListener {
                    onLoading(false)
                    start()
                }
                setOnErrorListener { _, what, extra ->
                    val errorMessage = when (what) {
                        MEDIA_ERROR_UNKNOWN -> {
                            "Unknown media error: $extra"
                        }

                        MEDIA_ERROR_SERVER_DIED -> {
                            "Media server died"
                        }

                        MEDIA_ERROR_MALFORMED -> {
                            "Malformed media: $extra"
                        }

                        MEDIA_ERROR_UNSUPPORTED -> {
                            "Unsupported media: $extra"
                        }

                        MEDIA_ERROR_IO -> {
                            "I/O error: $extra"
                        }

                        MEDIA_ERROR_TIMED_OUT -> {
                            "Timed out: $extra"
                        }

                        else -> {
                            "Unknown error: what=$what, extra=$extra"
                        }
                    }
                    onError(Throwable(errorMessage))
                    stopPlayback()
                    true
                }
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            if (stop) {
                view.stopPlayback()
            } else
                view.start()
        })
}