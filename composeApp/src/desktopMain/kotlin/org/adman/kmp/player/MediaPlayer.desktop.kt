package org.adman.kmp.player

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.io.File
import javax.swing.JPanel


fun initJavaFX() {
    System.setProperty("prism.order", "sw")
    System.setProperty("prism.verbose", "true")
}

@Composable
internal actual fun MediaPlayer(
    modifier: Modifier?,
    urlOrUri: String,
    isLandscape: Boolean,
    stop: Boolean,
    onLoading: (isLoading: Boolean) -> Unit,
    onError: (error: Throwable) -> Unit
) {
    val jPanel: JPanel = remember { JPanel() }
    val jfxPanel = JFXPanel()
    SwingPanel(
        factory = {
            jfxPanel.apply { buildWebView(urlOrUri, true, true, onLoading) }
            jPanel.add(jfxPanel)
        },
        modifier = modifier ?: Modifier.fillMaxWidth().height(300.dp),
    )
    DisposableEffect(true) { onDispose { jPanel.remove(jfxPanel) } }
}


private fun JFXPanel.buildWebView(
    url: String,
    autoPlay: Boolean,
    showControls: Boolean,
    onLoading: (isLoading: Boolean) -> Unit
) {
    initJavaFX()
    Platform.runLater {
        val webView = WebView()
        val webEngine = webView.engine

        webEngine.userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"

        webEngine.isJavaScriptEnabled = true
        val scene = Scene(webView)
        setScene(scene)

        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                // Script to remove overlays
                val removeOverlaysScript = """
                    setTimeout(function() {
                        var overlaySelectors = [
                            '.ytp-gradient-top',
                            '.ytp-gradient-bottom'
                        ];
                        overlaySelectors.forEach(function(selector) {
                            var element = document.querySelector(selector);
                            if (element !== null) {
                                element.style.display = 'none';
                            }
                        });
                    }, 1000);
                """.trimIndent()
                webEngine.executeScript(removeOverlaysScript)

                if (autoPlay) {
                    val autoPlayScript = """
                        setTimeout(function() {
                            var video = document.querySelector('video');
                            if (video) {
                                video.play();
                            }
                        }, 1000);
                    """.trimIndent()
                    webEngine.executeScript(autoPlayScript)
                    onLoading(false)
                    println("Autoplay enabled")
                }

                val toggleControlsScript = """
                    setTimeout(function() {
                        var video = document.querySelector('video');
                        if (video) {
                            video.controls = $showControls;
                        }
                    }, 1000);
                """.trimIndent()
                webEngine.executeScript(toggleControlsScript)
            }
        }

        webEngine.load(url.mediaUrl())
    }
}


private fun String.mediaUrl(): String {
    return if (this.startsWith("http") || this.startsWith("file://"))
        this
    else
        File(this).toURI().toString()
}