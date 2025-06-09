package com.oau.assess.repositories

import com.oau.assess.models.LoginRequest
import com.oau.assess.models.LoginResponse
import com.oau.assess.models.StudentData
import com.oau.assess.utils.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class StudentRepositoryImpl(
    private val client: HttpClient
) : StudentRepository {
    override suspend fun login(request: LoginRequest): NetworkResult<StudentData> {
        return try {
            val response: LoginResponse = client.post("${BASE_URL}student/auth") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(response.message)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    companion object {
        const val BASE_URL = "http://localhost:3000/"
    }
}
