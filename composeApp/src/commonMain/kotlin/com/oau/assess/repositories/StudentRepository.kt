package com.oau.assess.repositories

import com.oau.assess.models.LoginRequest
import com.oau.assess.models.StudentData
import com.oau.assess.utils.NetworkResult

interface StudentRepository {
    suspend fun login(request: LoginRequest): NetworkResult<StudentData>
}
