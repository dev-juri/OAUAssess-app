package com.oau.assess.screens.student.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.data.ExamAssignment
import com.oau.assess.models.StudentData
import com.oau.assess.repositories.StudentRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val exams: List<ExamAssignment>) : DashboardUiState()
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

    private val _examAssignments = MutableStateFlow<List<ExamAssignment>>(emptyList())
    val examAssignments: StateFlow<List<ExamAssignment>> = _examAssignments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shouldLogout = MutableStateFlow(false)
    val shouldLogout: StateFlow<Boolean> = _shouldLogout.asStateFlow()

    init {
        loadCurrentStudent()
    }

    fun loadCurrentStudent() {
        _isLoading.value = true
        val currentStudent = studentRepository.getCurrentStudent()
        _student.value = currentStudent

        if (currentStudent != null) {
            loadExamAssignments(currentStudent.id)
        } else {
            _isLoading.value = false
            _uiState.value = DashboardUiState.Empty
        }
    }

    private fun loadExamAssignments(studentId: String) {
        viewModelScope.launch {
            when (val result = studentRepository.getExamAssignments(studentId)) {
                is NetworkResult.Success -> {
                    _examAssignments.value = result.data
                    _uiState.value = if (result.data.isEmpty()) {
                        DashboardUiState.Empty
                    } else {
                        DashboardUiState.Success(result.data)
                    }
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    _uiState.value = DashboardUiState.Error(result.message)
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun logout() {
        studentRepository.clearCurrentStudent()
        _shouldLogout.value = true
    }

    fun onLogoutHandled() {
        _shouldLogout.value = false
    }

    fun retryLoadExams() {
        _student.value?.id?.let { studentId ->
            _isLoading.value = true
            loadExamAssignments(studentId)
        }
    }
}