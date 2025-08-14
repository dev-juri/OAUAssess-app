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

    @Serializable
    @SerialName("admin/exam-report")
    data class ExamReport(
        val examId: String,
        val examType: String
    ) : Screen()

    @Serializable
    @SerialName("/admin/create-exam")
    data object CreateExam : Screen()

    @Serializable
    @SerialName("admin/exam/mcq")
    data class UpdateMCQExam(
        val examId: String,
        val courseName: String,
        val courseCode: String,
        val duration: Int,
        val questionCount: Int,
        val examType: String
    ) : Screen()

    @Serializable
    @SerialName("admin/exam/oe")
    data class UpdateOpenEndedExam(
        val examId: String,
        val courseName: String,
        val courseCode: String,
        val duration: Int,
        val examType: String
    ) : Screen()
}