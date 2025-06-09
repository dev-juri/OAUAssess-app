package com.oau.assess.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    @SerialName("login")
    data object Login : Screen()

    @Serializable
    @SerialName("dashboard")
    data object Dashboard : Screen()
//
//    @Serializable
//    data class ExamDetail(val examId: String, val student: StudentData) : Screen()
}