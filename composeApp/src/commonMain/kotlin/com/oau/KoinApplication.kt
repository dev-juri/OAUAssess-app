package com.oau

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import com.oau.assess.di.appModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

//@Composable
//fun KoinApp(content: @Composable () -> Unit) {
//    KoinApplication(
//        application = {
//            modules(appModule)
//        }
//    ) {
//        content()
//    }
//}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

fun closeKoin() {
    stopKoin()
}
