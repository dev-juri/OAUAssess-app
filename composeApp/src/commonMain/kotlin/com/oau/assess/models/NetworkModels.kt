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
    val data: AdminToken? = null
)

@Serializable
data class AdminToken(
    val accessToken: String
)

@Serializable
data class CreateExamResponse(
    val success: Boolean,
    val message: String,
    val data: ExamData? = null
)

@Serializable
data class ExamData(
    val exam: Exam
)

@Serializable
data class Exam(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val duration: Int,
    val examType: String,
    val questionCount: Int,
    val questions: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    @SerialName("__v")
    val version: Int
)


@Serializable
data class ExamResponse(
    val success: Boolean,
    val message: String,
    val data: List<Exam>
)

@Serializable
data class UpdateExamResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ExamReportResponse(
    val success: Boolean,
    val message: String,
    val data: ExamReportData?
)

@Serializable
data class ExamReportData(
    val examTitle: String,
    val examId: String,
    val students: List<Student>
)

@Serializable
data class Student(
    val studentName: String,
    val matricNumber: String,
    val score: Int
)

@Serializable
data class UgrResponse(
    val message: String,
    val success: Boolean,
    val data: List<UngradedExam>?
)

@Serializable
data class UngradedExam(
    val examId: String,
    val courseName: String,
    val courseCode: String
)

@Serializable
data class GradeExamRequest(
    val examId: String
)

data class GradeExamResponse(
    val message: String,
    val success: Boolean
)