package com.oau.assess.repositories.student

import com.oau.assess.data.ExamAssignment
import com.oau.assess.data.Question
import com.oau.assess.models.LoginRequest
import com.oau.assess.models.QuestionResponse
import com.oau.assess.models.SubmissionResponse
import com.oau.assess.models.StudentData
import com.oau.assess.utils.ExamType
import com.oau.assess.utils.NetworkResult

interface StudentRepository {
    fun setCurrentStudent(student: StudentData)
    fun getCurrentStudent(): StudentData?
    fun clearCurrentStudent()

    suspend fun login(request: LoginRequest): NetworkResult<StudentData>
    suspend fun getExamAssignments(studentId: String): NetworkResult<List<ExamAssignment>>
    suspend fun getAssignmentQuestions(
        studentId: String,
        examId: String
    ): NetworkResult<List<Question>>

    suspend fun submitExam(
        examId: String,
        studentId: String,
        responses: List<QuestionResponse>,
        type: ExamType
    ): Result<SubmissionResponse>

}
