package com.oau.assess.repositories.admin

import com.oau.assess.models.AdminLoginRequest
import com.oau.assess.models.AdminLoginResponse
import com.oau.assess.models.AdminToken
import com.oau.assess.models.CreateExamResponse
import com.oau.assess.repositories.student.StudentRepositoryImpl.Companion.BASE_URL
import com.oau.assess.utils.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.khronos.webgl.ArrayBuffer
import org.w3c.files.File
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray

class AdminRepositoryImpl(private val client: HttpClient) : AdminRepository {

    private var currentLoggedInAdmin: String? = null

    override fun setLoggedInAdmin(token: String) {
        currentLoggedInAdmin = token
    }

    override fun getAdmin(): String? = currentLoggedInAdmin

    override fun clearCurrentAdmin() {
        currentLoggedInAdmin = null
    }

    override suspend fun login(request: AdminLoginRequest): NetworkResult<AdminToken> {
        return try {
            val response: AdminLoginResponse = client.post("${BASE_URL}auth") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            if (response.success) {
                setLoggedInAdmin(response.data!!.accessToken)
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(response.message)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun createExam(
        courseName: String,
        courseCode: String,
        duration: Int,
        questionCount: Int,
        examType: String,
        tutorialListFile: File
    ): NetworkResult<CreateExamResponse> {
        return try {
            // Convert browser File to ByteArray
            val fileBytes = Int8Array(tutorialListFile.unsafeCast<ArrayBuffer>()).toByteArray()

            val response = client.submitFormWithBinaryData(
                url = "http://localhost:3000/exam",
                formData = formData {
                    append("courseName", courseName)
                    append("courseCode", courseCode)
                    append("duration", duration.toString())
                    append("questionCount", questionCount.toString())
                    append("examType", examType)

                    // Alternative: Use ChannelProvider
                    appendInput("tutorialList", Headers.build {
                        append(HttpHeaders.ContentType, tutorialListFile.type)
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"tutorialList\"; filename=\"${tutorialListFile.name}\""
                        )
                    }) {
                        buildPacket { writeFully(fileBytes) }
                    }
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            NetworkResult.Success(response.body())
        } catch (e: Exception) {
            NetworkResult.Error(e.message.toString())
        }
    }
}