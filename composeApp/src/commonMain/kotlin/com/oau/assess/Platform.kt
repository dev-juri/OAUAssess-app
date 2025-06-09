package com.oau.assess

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform