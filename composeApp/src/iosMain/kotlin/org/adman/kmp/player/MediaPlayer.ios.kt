package org.adman.kmp.player

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.uikit.InterfaceOrientation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIInterfaceOrientation
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.setNeedsUpdateOfSupportedInterfaceOrientations
import platform.UIKit.setStatusBarOrientation
import platform.darwin.NSObject

const val playerDefaultMinHeight = 300.0

@OptIn(ExperimentalForeignApi::class, InternalComposeUiApi::class)
@Composable
internal actual fun MediaPlayer(
    modifier: Modifier?,
    urlOrUri: String,
    isLandscape: Boolean,
    stop: Boolean,
    onLoading: (isLoading: Boolean) -> Unit,
    onError: (error: Throwable) -> Unit
) {

    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

    val player = remember { AVPlayer(uRL = NSURL.URLWithString(urlOrUri)!!) }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true
    playerLayer.player = player

    // Progress
    DisposableEffect(Unit) {
        val observer =
            player.addPeriodicTimeObserverForInterval(CMTimeMake(value = 1, timescale = 1), null) {
                if (player.currentItem?.status == AVPlayerItemStatusReadyToPlay) {
                    onLoading(false)
                }
            }

        onDispose {
            player.removeTimeObserver(observer)
        }
    }

    // Set the orientation based on isLandscape parameter
    var currentOrientation by remember { mutableStateOf(if (isLandscape) InterfaceOrientation.LandscapeRight else InterfaceOrientation.Portrait) }
    LaunchedEffect(isLandscape) {
        if (isLandscape) {
            if (currentOrientation != InterfaceOrientation.LandscapeRight) {
                UIDevice.currentDevice.setValue(
                    InterfaceOrientation.LandscapeRight,
                    forKey = "orientation"
                )
                UIApplication.sharedApplication.setStatusBarOrientation(
                    UIInterfaceOrientation.MAX_VALUE,
                    animated = true
                )
                currentOrientation = InterfaceOrientation.LandscapeRight
            }
            enterPlayerFullScreen(
                UIDeviceOrientation.UIDeviceOrientationLandscapeRight,
                avPlayerViewController,
                rootViewController
            )
        } else {
            if (currentOrientation != InterfaceOrientation.Portrait) {
                UIDevice.currentDevice.setValue(
                    InterfaceOrientation.Portrait,
                    forKey = "orientation"
                )
                UIApplication.sharedApplication.setStatusBarOrientation(
                    UIInterfaceOrientation.MAX_VALUE,
                    animated = true
                )
                currentOrientation = InterfaceOrientation.Portrait
            }
            exitPlayerFullScreen(avPlayerViewController)
        }
    }

    val currentDeviceOrientation = rememberDeviceOrientation()
    LaunchedEffect(currentDeviceOrientation) {
        enterPlayerFullScreen(currentDeviceOrientation, avPlayerViewController, rootViewController)
    }

    UIKitView(
        factory = {
            // Forcing the player to respect the default height
            val playerContainer = object : UIView(CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    super.layoutSubviews()
                    val rect = this.bounds
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)
                    avPlayerViewController.view.setFrame(rect)
                    playerLayer.frame = rect
                    CATransaction.commit()
                }
            }

            playerContainer.addSubview(avPlayerViewController.view)

            NSLayoutConstraint.activateConstraints(
                listOf(
                    avPlayerViewController.view.topAnchor.constraintEqualToAnchor(playerContainer.topAnchor),
                    avPlayerViewController.view.leadingAnchor.constraintEqualToAnchor(
                        playerContainer.leadingAnchor
                    ),
                    avPlayerViewController.view.trailingAnchor.constraintEqualToAnchor(
                        playerContainer.trailingAnchor
                    ),
                    avPlayerViewController.view.bottomAnchor.constraintEqualToAnchor(playerContainer.bottomAnchor)
                )
            )

            playerContainer
        },
        update = { _ ->
            if (stop) {
                player.pause()
                avPlayerViewController.player!!.play()
                player.seekToTime(CMTimeMake(value = 0, timescale = 1))
            } else {
                player.play()
            }
        },
        modifier = (Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = playerDefaultMinHeight.dp)
            .then(modifier ?: Modifier)),
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}


private fun enterPlayerFullScreen(
    currentDeviceOrientation: UIDeviceOrientation,
    avPlayerViewController: AVPlayerViewController,
    rootViewController: UIViewController?
) {
    if (currentDeviceOrientation == UIDeviceOrientation.UIDeviceOrientationLandscapeLeft
        || currentDeviceOrientation == UIDeviceOrientation.UIDeviceOrientationLandscapeRight
    )
        rootViewController?.presentViewController(
            avPlayerViewController,
            animated = true,
            completion = null
        )
}

@OptIn(InternalComposeUiApi::class)
private fun exitPlayerFullScreen(avPlayerViewController: AVPlayerViewController) {
    // Force portrait mode
    UIDevice.currentDevice.setValue(InterfaceOrientation.Portrait, forKey = "orientation")

    // Notify the system to update interface orientations
    avPlayerViewController.setNeedsUpdateOfSupportedInterfaceOrientations()
}


class OrientationObserver(private val onOrientationChange: (UIDeviceOrientation) -> Unit) :
    NSObject() {

    @ObjCAction
    fun orientationChanged() {
        onOrientationChange(UIDevice.currentDevice.orientation)
    }
}


@OptIn(ExperimentalForeignApi::class)
@Composable
fun rememberDeviceOrientation(): UIDeviceOrientation {
    var orientation by remember { mutableStateOf(UIDevice.currentDevice.orientation) }

    DisposableEffect(Unit) {
        val observer = OrientationObserver { newOrientation ->
            orientation = newOrientation
        }

        val notificationCenter = NSNotificationCenter.defaultCenter
        notificationCenter.addObserver(
            observer,
            NSSelectorFromString("orientationChanged"),
            "UIDeviceOrientationDidChangeNotification",
            null
        )

        onDispose {
            notificationCenter.removeObserver(observer)
        }
    }

    return orientation
}




