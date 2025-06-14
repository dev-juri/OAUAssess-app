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
