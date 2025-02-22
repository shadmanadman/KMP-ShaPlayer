package org.adman.kmp.player

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {KmpShaPlayer(urlOrPathToFile = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }