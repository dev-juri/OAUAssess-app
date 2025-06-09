package com.oau.assess.screens.student.dashboard

import androidx.lifecycle.ViewModel
import com.oau.assess.models.StudentData
import com.oau.assess.repositories.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success<T>(val data: T) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
    object Empty : DashboardUiState()
}

class DashboardViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _student = MutableStateFlow<StudentData?>(null)
    val student: StateFlow<StudentData?> = _student.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shouldLogout = MutableStateFlow(false)
    val shouldLogout: StateFlow<Boolean> = _shouldLogout.asStateFlow()

    init {
        loadCurrentStudent()
    }

    private fun loadCurrentStudent() {
        _isLoading.value = true
        val currentStudent = studentRepository.getCurrentStudent()
        _student.value = currentStudent
        _isLoading.value = false

        if (currentStudent != null) {
            _uiState.value = DashboardUiState.Success(currentStudent)
        } else {
            _uiState.value = DashboardUiState.Empty
        }
    }

    fun logout() {
        studentRepository.clearCurrentStudent()
        _shouldLogout.value = true
    }

    fun onLogoutHandled() {
        _shouldLogout.value = false
    }
}