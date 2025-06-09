// wasmJsMain or jsMain source set
package com.oau.assess

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import com.oau.initKoin
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalBrowserHistoryApi::class
)
fun main() {

    initKoin()

    val body = document.body ?: return
    ComposeViewport(body) {
        App(
            onNavHostReady = { window.bindToNavigation(it) }
        )
    }
}