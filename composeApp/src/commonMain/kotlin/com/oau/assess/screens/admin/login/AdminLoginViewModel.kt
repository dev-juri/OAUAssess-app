package com.oau.assess.screens.admin.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.models.AdminLoginRequest
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val token: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class AdminLoginViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onEmailChanged(value: String) {
        email.value = value
        _errorMessage.value = null
    }

    fun onPasswordChanged(value: String) {
        password.value = value
        _errorMessage.value = null
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = repository.login(AdminLoginRequest(email.value, password.value))) {
                is NetworkResult.Success -> {
                    repository.setLoggedInAdmin(result.data.accessToken)
                    _uiState.value = LoginUiState.Success(result.data.accessToken)
                }

                is NetworkResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                    _errorMessage.value = result.message
                }

                is NetworkResult.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}