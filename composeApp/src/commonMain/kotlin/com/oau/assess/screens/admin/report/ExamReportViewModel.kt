package com.oau.assess.screens.admin.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.models.Student
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamReportViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamReportUiState())
    val uiState: StateFlow<ExamReportUiState> = _uiState.asStateFlow()

    fun loadExamReport(examId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                examId = examId
            )

            when (val result = adminRepository.getExamReport(examId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        examTitle = result.data.examTitle,
                        examId = result.data.examId,
                        students = result.data.students,
                        error = null
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    fun downloadExamReport(examId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingReport = true,
                downloadError = null
            )

            when (val result = adminRepository.downloadExamReport(examId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingReport = false,
                        reportDownloadSuccess = true
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingReport = false,
                        downloadError = result.message
                    )
                }

                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isDownloadingReport = true)
                }
            }
        }
    }

    fun downloadExamScripts(examId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingScripts = true,
                downloadError = null
            )

            when (val result = adminRepository.downloadExamScripts(examId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingScripts = false,
                        scriptsDownloadSuccess = true
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingScripts = false,
                        downloadError = result.message
                    )
                }

                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isDownloadingScripts = true)
                }
            }
        }
    }

    fun clearDownloadSuccess() {
        _uiState.value = _uiState.value.copy(
            reportDownloadSuccess = false,
            scriptsDownloadSuccess = false
        )
    }

    fun clearDownloadError() {
        _uiState.value = _uiState.value.copy(downloadError = null)
    }

    fun retryLoading() {
        val currentExamId = _uiState.value.examId
        if (currentExamId.isNotEmpty()) {
            loadExamReport(currentExamId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ExamReportUiState(
    val isLoading: Boolean = false,
    val examTitle: String = "",
    val examId: String = "",
    val students: List<Student> = emptyList(),
    val error: String? = null,
    val isDownloadingReport: Boolean = false,
    val isDownloadingScripts: Boolean = false,
    val reportDownloadSuccess: Boolean = false,
    val scriptsDownloadSuccess: Boolean = false,
    val downloadError: String? = null
)