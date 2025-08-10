package com.oau.assess.screens.admin.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.w3c.files.File

sealed class ExamUpdateUiState {
    data object Loading : ExamUpdateUiState()
    data object Success : ExamUpdateUiState()
    data class Error(val message: String) : ExamUpdateUiState()
    data object Idle : ExamUpdateUiState()
}

data class SelectedFiles(
    val mcqFile: File? = null,
    val questionsFile: File? = null,
    val answerKeyFile: File? = null
)


class ExamUpdateViewModel(private val adminRepository: AdminRepository) : ViewModel() {

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _uiState = MutableStateFlow<ExamUpdateUiState>(ExamUpdateUiState.Idle)
    val uiState: StateFlow<ExamUpdateUiState> = _uiState.asStateFlow()

    private val _selectedFiles = MutableStateFlow(SelectedFiles())
    val selectedFiles: StateFlow<SelectedFiles> = _selectedFiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAdminLoginStatus()
    }

    fun selectMcqFile(file: File?) {
        _selectedFiles.value = _selectedFiles.value.copy(mcqFile = file)
    }

    fun selectQuestionsFile(file: File?) {
        _selectedFiles.value = _selectedFiles.value.copy(questionsFile = file)
    }

    fun selectAnswerKeyFile(file: File?) {
        _selectedFiles.value = _selectedFiles.value.copy(answerKeyFile = file)
    }

    fun checkAdminLoginStatus() {
        val admin = adminRepository.getAdmin()
        _isAdminLoggedIn.value = !admin.isNullOrEmpty()
    }

    fun updateMcqExam(examId: String) {
        val mcqFile = _selectedFiles.value.mcqFile
        if (mcqFile == null) {
            _uiState.value = ExamUpdateUiState.Error("Please select an Excel file")
            return
        }

        if (!_isAdminLoggedIn.value) {
            _uiState.value = ExamUpdateUiState.Error("Admin not logged in")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ExamUpdateUiState.Loading

            when (val result = adminRepository.updateMcqExam(examId, mcqFile)) {
                is NetworkResult.Success -> {
                    _uiState.value = ExamUpdateUiState.Success
                    clearSelectedFiles()
                }

                is NetworkResult.Error -> {
                    _uiState.value = ExamUpdateUiState.Error(result.message)
                }

                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
            _isLoading.value = false
        }
    }

    fun logoutAdmin() {
        adminRepository.clearCurrentAdmin()
        _isAdminLoggedIn.value = false
        resetState()
    }

    fun updateOeExam(examId: String) {
        val questionsFile = _selectedFiles.value.questionsFile
        val answerKeyFile = _selectedFiles.value.answerKeyFile

        if (questionsFile == null && answerKeyFile == null) {
            _uiState.value = ExamUpdateUiState.Error("Please select at least one file")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ExamUpdateUiState.Loading

            val templateFiles = listOfNotNull(questionsFile, answerKeyFile)

            when (val result = adminRepository.updateOeExam(examId, templateFiles)) {
                is NetworkResult.Success -> {
                    _uiState.value = ExamUpdateUiState.Success
                    clearSelectedFiles()
                }

                is NetworkResult.Error -> {
                    _uiState.value = ExamUpdateUiState.Error(result.message)
                }

                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
            _isLoading.value = false
        }
    }

    fun clearSelectedFiles() {
        _selectedFiles.value = SelectedFiles()
    }

    fun clearError() {
        if (_uiState.value is ExamUpdateUiState.Error) {
            _uiState.value = ExamUpdateUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = ExamUpdateUiState.Idle
        clearSelectedFiles()
    }
}