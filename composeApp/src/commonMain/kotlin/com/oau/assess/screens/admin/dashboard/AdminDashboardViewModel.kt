package com.oau.assess.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.models.Exam
import com.oau.assess.models.ExamData
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminDashboardUiState {
    object Loading : AdminDashboardUiState()
    data class Success(val admin: String, val exams: List<Exam>) : AdminDashboardUiState()
    data class Error(val message: String) : AdminDashboardUiState()
    object Empty : AdminDashboardUiState()
}

class AdminDashboardViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shouldLogout = MutableStateFlow(false)
    val shouldLogout: StateFlow<Boolean> = _shouldLogout.asStateFlow()

    private val _admin = MutableStateFlow<String?>(null)
    val admin: StateFlow<String?> = _admin.asStateFlow()

    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    init {
        loadLoggedInAdmin()
    }

    fun loadLoggedInAdmin() {
        _isLoading.value = true
        val currentAdmin = repository.getAdmin()
        _admin.value = currentAdmin

        if (currentAdmin != null) {
            loadExams()
        } else {
            _isLoading.value = false
            _uiState.value = AdminDashboardUiState.Empty
        }
    }

    private fun loadExams() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.getAllExams()) {
                is NetworkResult.Success -> {
                    val examList = result.data
                    _exams.value = examList
                    _uiState.value = AdminDashboardUiState.Success(
                        admin = _admin.value ?: "",
                        exams = examList
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = AdminDashboardUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    // Keep current loading state
                }
            }

            _isLoading.value = false
        }
    }

    fun refreshExams() {
        loadExams()
    }

    fun logout() {
        repository.clearCurrentAdmin()
        _shouldLogout.value = true
    }

    fun onLogoutHandled() {
        _shouldLogout.value = false
    }
}