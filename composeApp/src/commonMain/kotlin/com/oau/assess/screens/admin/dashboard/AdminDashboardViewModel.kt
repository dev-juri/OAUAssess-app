package com.oau.assess.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import com.oau.assess.data.ExamAssignment
import com.oau.assess.models.StudentData
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.screens.student.dashboard.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AdminDashboardUiState {
    object Loading : AdminDashboardUiState()
    data class Success(val admin: String) : AdminDashboardUiState()
    data class Error(val message: String) : AdminDashboardUiState()
    object Empty : AdminDashboardUiState()
}


class AdminDashboardViewModel (
    private val repository: AdminRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shouldLogout = MutableStateFlow(false)
    val shouldLogout: StateFlow<Boolean> = _shouldLogout.asStateFlow()

    private val _admin = MutableStateFlow<String?>(null)
    val admin: StateFlow<String?> = _admin.asStateFlow()

    init {
        loadLoggedInAdmin()
    }

    fun loadLoggedInAdmin() {
        _isLoading.value = true
        val currentAdmin = repository.getAdmin()
        _admin.value = currentAdmin

        if (currentAdmin != null) {
        } else {
            _isLoading.value = false
            _uiState.value = DashboardUiState.Empty
        }
    }

    fun logout() {
        repository.clearCurrentAdmin()
        _shouldLogout.value = true
    }

    fun onLogoutHandled() {
        _shouldLogout.value = false
    }

}