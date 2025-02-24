package org.adman.kmp.player

import HtmlView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event

@Composable
internal actual fun MediaPlayer(
    modifier: Modifier?,
    urlOrUri: String,
    isLandscape: Boolean,
    stop: Boolean,
    onLoading: (isLoading: Boolean) -> Unit,
    onError: (error: Throwable) -> Unit
) {
    Column(
        modifier = modifier ?: Modifier.fillMaxSize().height(300.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HtmlView(
            modifier = Modifier.fillMaxWidth().height(if (isLandscape) 540.dp else 300.dp),
            factory = {
                val video = createElement("video") as HTMLVideoElement

                video.apply {
                    setAttribute("width", "100%")
                    setAttribute("height", "100%")
                    setAttribute("src", urlOrUri)
                    setAttribute("controls", "true")
                    setAttribute("autoplay", "true")
                    setAttribute("crossorigin", "anonymous")
                    setAttribute("paused", stop.toString())

                    addEventListener("loadeddata", {
                        onLoading(false)
                        println("Video loaded successfully")
                    })
                    addEventListener("loadstart", {
                        onLoading(true)
                        println("Loading video...")
                    })
                    addEventListener("error", { event: Event ->
                        onLoading(false)
                        onError(Throwable("Failed to load video: ${event.toJsReference()}"))
                    })
                    if (stop)
                        pause()
                }
                video
            }
        )
    }
}

