package com.oau.assess.repositories.admin

import com.oau.assess.models.AdminLoginRequest
import com.oau.assess.models.AdminLoginResponse
import com.oau.assess.models.AdminToken
import com.oau.assess.models.CreateExamResponse
import com.oau.assess.utils.NetworkResult
import com.oau.assess.utils.readFileAsByteArray
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.js.Js
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import org.w3c.files.File

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


    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun createExam(
        courseName: String,
        courseCode: String,
        duration: Int,
        questionCount: Int,
        examType: String,
        tutorialListFile: File
    ): NetworkResult<CreateExamResponse> {
        return try {
            val fileBytes = readFileAsByteArray(tutorialListFile)

            val response = HttpClient(Js).submitFormWithBinaryData(
                url = "${BASE_URL}exam",
                formData = formData {
                    append("courseName", courseName)
                    append("courseCode", courseCode)
                    append("duration", duration.toString())
                    append("questionCount", questionCount.toString())
                    append("examType", examType)
                    append("tutorialList", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"tutorial_list.xlsx\"")
                        append(HttpHeaders.ContentType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            NetworkResult.Success(response.body())

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }


    companion object {
        private const val BASE_URL = "http://localhost:3000/"
    }
}