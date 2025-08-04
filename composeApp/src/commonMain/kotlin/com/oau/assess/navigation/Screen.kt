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

    @Serializable
    @SerialName("mcq")
    data class McqExam(
        val studentId: String,
        val examId: String,
        val examTitle: String,
        val duration: Int
    ) : Screen()

    @Serializable
    @SerialName("oe")
    data class OpenEndedExam(
        val studentId: String,
        val examId: String,
        val examTitle: String,
        val duration: Int
    ) : Screen()

    // Admin Flow
    @Serializable
    @SerialName("admin/auth")
    data object AdminLogin : Screen()

    @Serializable
    @SerialName("admin/dashboard")
    data object AdminDashboard : Screen()
}