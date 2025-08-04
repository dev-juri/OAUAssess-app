package com.oau.assess.repositories

import com.oau.assess.data.AssignmentQuestionsResponse
import com.oau.assess.data.ExamAssignment
import com.oau.assess.data.ExamAssignmentsResponse
import com.oau.assess.data.Question
import com.oau.assess.models.LoginRequest
import com.oau.assess.models.LoginResponse
import com.oau.assess.models.QuestionResponse
import com.oau.assess.models.SubmissionRequest
import com.oau.assess.models.SubmissionResponse
import com.oau.assess.models.StudentData
import com.oau.assess.utils.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class StudentRepositoryImpl(
    private val client: HttpClient
) : StudentRepository {

    private var currentStudent: StudentData? = null

    override fun setCurrentStudent(student: StudentData) {
        currentStudent = student
    }

    override fun getCurrentStudent(): StudentData? = currentStudent

    override fun clearCurrentStudent() {
        currentStudent = null
    }

    override suspend fun login(request: LoginRequest): NetworkResult<StudentData> {
        return try {
            val response: LoginResponse = client.post("${BASE_URL}student/auth") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            if (response.success && response.data != null) {
                setCurrentStudent(response.data)
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(response.message)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getExamAssignments(studentId: String): NetworkResult<List<ExamAssignment>> {
        return try {
            val response = client.get("${BASE_URL}student/$studentId/assignments")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val assignmentsResponse = response.body<ExamAssignmentsResponse>()
                    if (assignmentsResponse.success) {
                        NetworkResult.Success(assignmentsResponse.data)
                    } else {
                        NetworkResult.Error(assignmentsResponse.message)
                    }
                }
                else -> {
                    NetworkResult.Error("Failed to fetch exam assignments")
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An error occurred")
        }
    }

    override suspend fun getAssignmentQuestions(
        studentId: String,
        examId: String
    ): NetworkResult<List<Question>> {
        return try {
            val response = client.get("${BASE_URL}student/$studentId/assignments/$examId")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val questionsResponse = response.body<AssignmentQuestionsResponse>()
                    if (questionsResponse.success) {
                        NetworkResult.Success(questionsResponse.data)
                    } else {
                        NetworkResult.Error(questionsResponse.message)
                    }
                }
                else -> {
                    NetworkResult.Error("Failed to fetch assignment questions")
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An error occurred")
        }
    }

    override suspend fun submitMcqExam(
        examId: String,
        studentId: String,
        responses: List<QuestionResponse>
    ): Result<SubmissionResponse> {
        return try {
            val request = SubmissionRequest(
                examId = examId,
                studentId = studentId,
                responses = responses
            )

            val response = client.post("${BASE_URL}student/submit/mcq") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val submissionResponse = response.body<SubmissionResponse>()
                    Result.success(submissionResponse)
                }
                else -> {
                    Result.failure(Exception("Failed to submit exam: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val BASE_URL = "http://localhost:3000/"
    }
}
