package org.adman.kmp.player

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform