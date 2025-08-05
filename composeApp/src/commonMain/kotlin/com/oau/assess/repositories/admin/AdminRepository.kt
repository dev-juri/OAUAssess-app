package com.oau.assess.repositories.admin

import com.oau.assess.models.AdminLoginRequest
import com.oau.assess.models.AdminToken
import com.oau.assess.models.CreateExamResponse
import com.oau.assess.utils.NetworkResult
import org.w3c.files.File

interface AdminRepository {

    fun setLoggedInAdmin(token: String)
    fun getAdmin(): String?
    fun clearCurrentAdmin()

    suspend fun login(request: AdminLoginRequest): NetworkResult<AdminToken>

    suspend fun createExam(
        courseName: String,
        courseCode: String,
        duration: Int,
        questionCount: Int,
        examType: String,
        tutorialListFile: File
    ): NetworkResult<CreateExamResponse>
}