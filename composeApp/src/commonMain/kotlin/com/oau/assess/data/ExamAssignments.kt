package com.oau.assess.data

import kotlinx.serialization.Serializable

@Serializable
data class ExamAssignment(
    val examId: String,
    val courseName: String,
    val courseCode: String,
    val duration: Int,
    val examType: String
)

@Serializable
data class ExamAssignmentsResponse(
    val success: Boolean,
    val message: String,
    val data: List<ExamAssignment>
)