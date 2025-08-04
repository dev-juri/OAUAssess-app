package com.oau.assess.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val matricNo: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: StudentData? = null
)

@Serializable
data class StudentData(
    val id: String,
    val matricNo: String,
    val fullName: String,
    val createdAt: String,
    val updatedAt: String,
    @SerialName("__v")
    val version: Int
)

@Serializable
data class SubmissionRequest(
    val examId: String,
    val studentId: String,
    val responses: List<QuestionResponse>
)

@Serializable
data class QuestionResponse(
    val questionId: String,
    val answer: String
)

@Serializable
data class SubmissionResponse(
    val success: Boolean,
    val message: String
)


// Admin Models
@Serializable
data class AdminLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AdminLoginResponse(
    val success: Boolean,
    val message: String,
    val data: AdminToken
)

@Serializable
data class AdminToken(
    val accessToken: String
)