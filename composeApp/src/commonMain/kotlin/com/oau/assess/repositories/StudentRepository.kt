package com.oau.assess.repositories

import com.oau.assess.data.ExamAssignment
import com.oau.assess.models.LoginRequest
import com.oau.assess.models.StudentData
import com.oau.assess.utils.NetworkResult

interface StudentRepository {
    suspend fun login(request: LoginRequest): NetworkResult<StudentData>
    fun setCurrentStudent(student: StudentData)
    fun getCurrentStudent(): StudentData?
    fun clearCurrentStudent()

    suspend fun getExamAssignments(studentId: String): NetworkResult<List<ExamAssignment>>
}
