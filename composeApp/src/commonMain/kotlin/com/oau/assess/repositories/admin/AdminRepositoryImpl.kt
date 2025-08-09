package com.oau.assess.repositories.admin

import com.oau.assess.models.AdminLoginRequest
import com.oau.assess.models.AdminLoginResponse
import com.oau.assess.models.AdminToken
import com.oau.assess.models.CreateExamResponse
import com.oau.assess.models.Exam
import com.oau.assess.models.ExamResponse
import com.oau.assess.utils.NetworkResult
import com.oau.assess.utils.readFileAsByteArray
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import com.oau.assess.models.ExamData
import com.oau.assess.models.UpdateExamResponse
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.json.Json
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

            val response: CreateExamResponse = HttpClient(Js) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }.submitFormWithBinaryData(
                url = "${BASE_URL}exam",
                formData = formData {
                    append("courseName", courseName)
                    append("courseCode", courseCode)
                    append("duration", duration.toString())
                    append("questionCount", questionCount.toString())
                    append("examType", examType)
                    append("tutorialList", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"tutorial_list.xlsx\"")
                        append(
                            HttpHeaders.ContentType,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }.body()

            NetworkResult.Success(response)

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getAllExams(): NetworkResult<List<Exam>> {
        return try {
            val response = client.get("${BASE_URL}exam/all") {
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            if (response.status.isSuccess()) {
                val examResponse: ExamResponse = response.body()
                if (examResponse.success) {
                    NetworkResult.Success(examResponse.data)
                } else {
                    NetworkResult.Error(examResponse.message)
                }
            } else {
                NetworkResult.Error("Failed to fetch exams: ${response.status}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun updateMcqExam(
        examId: String,
        mcqFile: File
    ): NetworkResult<UpdateExamResponse> {
        return try {
            val fileBytes = readFileAsByteArray(mcqFile)

            val response = HttpClient(Js) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }.submitFormWithBinaryData(
                url = "${BASE_URL}exam/mcq/$examId",
                formData = formData {
                    append("mcqList", fileBytes, Headers.build {
                        append(
                            HttpHeaders.ContentType,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                        append(HttpHeaders.ContentDisposition, "filename=\"${mcqFile.name}\"")
                    })
                }
            ) {
                method = HttpMethod.Patch
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            if (response.status.isSuccess()) {
                val updateResponse: UpdateExamResponse = response.body()
                NetworkResult.Success(updateResponse)
            } else {
                NetworkResult.Error("Failed to update MCQ exam: ${response.status}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun updateOeExam(
        examId: String,
        templateFiles: List<File>
    ): NetworkResult<UpdateExamResponse> {
        return try {
            val fileMap: HashMap<String, ByteArray> = HashMap()

            templateFiles.forEach { file ->
                when {
                    file.name.endsWith("docx") ->
                        fileMap["application/vnd.openxmlformats-officedocument.wordprocessingml.document"] =
                            readFileAsByteArray(file)

                    file.name.endsWith("xlsx") ->
                        fileMap["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"] =
                            readFileAsByteArray(file)

                    else -> fileMap["application/octet-stream"] = readFileAsByteArray(file)
                }
            }

            val response = HttpClient(Js) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }.submitFormWithBinaryData(
                url = "${BASE_URL}exam/oe/$examId",
                formData = formData {
                    fileMap.forEach { file ->

                        append("templates", file.value, Headers.build {
                            append(HttpHeaders.ContentType, file.key)
                            append(HttpHeaders.ContentDisposition, "filename=\"oe\"")
                        })
                    }
                }
            ) {
                method = HttpMethod.Patch
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            if (response.status.isSuccess()) {
                val updateResponse: UpdateExamResponse = response.body()
                NetworkResult.Success(updateResponse)
            } else {
                NetworkResult.Error("Failed to update OE exam: ${response.status}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    companion object {
        private const val BASE_URL = "http://localhost:3000/"
    }
}