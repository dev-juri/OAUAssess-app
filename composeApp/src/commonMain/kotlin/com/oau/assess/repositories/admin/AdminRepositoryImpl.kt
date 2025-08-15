package com.oau.assess.repositories.admin

import com.oau.assess.BuildConfig
import com.oau.assess.models.AdminLoginRequest
import com.oau.assess.models.AdminLoginResponse
import com.oau.assess.models.AdminToken
import com.oau.assess.models.CreateExamResponse
import com.oau.assess.models.Exam
import com.oau.assess.models.ExamReportData
import com.oau.assess.models.ExamReportResponse
import com.oau.assess.models.ExamResponse
import com.oau.assess.models.UpdateExamResponse
import com.oau.assess.utils.FileManager
import com.oau.assess.utils.NetworkResult
import com.oau.assess.utils.extractFilenameFromHeader
import com.oau.assess.utils.readFileAsByteArray
import com.oau.assess.utils.saveFile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.toByteArray
import js.typedarrays.toUint8Array
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
            val response: AdminLoginResponse = client.post("${BuildConfig.BASE_URL}auth") {
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
                url = "${BuildConfig.BASE_URL}exam",
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
            }

            if (response.status.isSuccess()) {
                val response: CreateExamResponse = response.body()
                NetworkResult.Success(response)
            } else {
                if (response.status.value == 401) {
                    clearCurrentAdmin()
                }
                NetworkResult.Error("Failed to create MCQ exam: ${response.status}")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getAllExams(): NetworkResult<List<Exam>> {
        return try {
            val response = client.get("${BuildConfig.BASE_URL}exam/all") {
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
                if (response.status.value == 401) {
                    clearCurrentAdmin()
                }
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
                url = "${BuildConfig.BASE_URL}exam/mcq/$examId",
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
                if (response.status.value == 401) {
                    clearCurrentAdmin()
                }
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
            val fileManagerList = mutableListOf<FileManager>()

            templateFiles.forEach { file ->
                when {
                    file.name.endsWith("docx") -> {
                        fileManagerList.add(
                            FileManager(
                                fileName = file.name,
                                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                fileContent = readFileAsByteArray(file)
                            )
                        )
                    }

                    file.name.endsWith("pdf") -> {
                        fileManagerList.add(
                            FileManager(
                                fileName = file.name,
                                mimeType = "application/pdf",
                                fileContent = readFileAsByteArray(file)
                            )
                        )
                    }

                    file.name.endsWith("xlsx") -> {
                        fileManagerList.add(
                            FileManager(
                                fileName = file.name,
                                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                fileContent = readFileAsByteArray(file)
                            )
                        )
                    }

                    else -> {
                        fileManagerList.add(
                            FileManager(
                                fileName = file.name,
                                mimeType = "application/octet-stream",
                                fileContent = readFileAsByteArray(file)
                            )
                        )
                    }
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
                url = "${BuildConfig.BASE_URL}exam/oe/$examId",
                formData = formData {
                    fileManagerList.forEach { file ->

                        append("templates", file.fileContent, Headers.build {
                            append(HttpHeaders.ContentType, file.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.fileName}\"")
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
                if (response.status.value == 401) {
                    clearCurrentAdmin()
                }
                NetworkResult.Error("Failed to update OE exam: ${response.status}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getExamReport(examId: String): NetworkResult<ExamReportData> {
        return try {
            val response = client.get {
                url("${BuildConfig.BASE_URL}exam/$examId/report")
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val examReportResponse = response.body<ExamReportResponse>()
                    if (examReportResponse.success) {
                        NetworkResult.Success(examReportResponse.data!!)
                    } else {
                        if (response.status.value == 401) {
                            clearCurrentAdmin()
                        }
                        NetworkResult.Error(examReportResponse.message)
                    }
                }

                else -> {
                    NetworkResult.Error("Failed to fetch exam report: ${response.status}")
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message.toString())
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun downloadExamReport(examId: String): NetworkResult<String> {
        return try {
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
            }.get {
                url("${BuildConfig.BASE_URL}exam/$examId/report/download")
                contentType(ContentType.Application.Xlsx)
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
                header(HttpHeaders.ContentDisposition, "")
            }

            if (response.status.isSuccess() || response.status == HttpStatusCode.NotModified) {
                val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
                    ?: response.headers["content-disposition"] // Fallback to lowercase
                    ?: response.headers.entries().firstOrNull {
                        it.key.equals(
                            "Content-Disposition",
                            ignoreCase = true
                        )
                    }?.value?.firstOrNull()

                println(response.headers)
                val filename = extractFilenameFromHeader(contentDisposition, true)

                val fileData = response.bodyAsChannel().toByteArray()

                saveFile(fileData.toUint8Array().unsafeCast(), filename)

                NetworkResult.Success("Successfully downloaded report: $filename")
            } else {
                when (response.status.value) {
                    401 -> {
                        clearCurrentAdmin()
                        NetworkResult.Error("Unauthorized")
                    }

                    else -> NetworkResult.Error("Failed to download report: ${response.status.description}")
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred while downloading report")
        }
    }


    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun downloadExamScripts(examId: String): NetworkResult<String> {
        return try {
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
            }.get {
                url("${BuildConfig.BASE_URL}exam/$examId/report/download-scripts")
                contentType(ContentType.Application.Zip)
                header(HttpHeaders.Authorization, "Bearer $currentLoggedInAdmin")
            }

            if (response.status.isSuccess() || response.status == HttpStatusCode.NotModified) {
                val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
                    ?: response.headers["content-disposition"]
                    ?: response.headers.entries().firstOrNull {
                        it.key.equals(
                            "Content-Disposition",
                            ignoreCase = true
                        )
                    }?.value?.firstOrNull()

                val filename = extractFilenameFromHeader(contentDisposition, false)

                val fileData = response.bodyAsChannel().toByteArray()

                saveFile(fileData.toUint8Array().unsafeCast(), filename)

                NetworkResult.Success("Successfully downloaded report: $filename")
            } else {
                when (response.status.value) {
                    401 -> {
                        clearCurrentAdmin()
                        NetworkResult.Error("Unauthorized")
                    }

                    else -> NetworkResult.Error("Failed to download report: ${response.status.description}")
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred while downloading scripts")
        }
    }

}