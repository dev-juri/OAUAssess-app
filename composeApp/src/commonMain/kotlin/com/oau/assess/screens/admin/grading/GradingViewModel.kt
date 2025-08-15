package com.oau.assess.screens.admin.grading


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.models.GradeExamRequest
import com.oau.assess.models.UngradedExam
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class GradingUiState {
    object Loading : GradingUiState()
    data class Success(val exams: List<UngradedExam>) : GradingUiState()
    data class Error(val message: String) : GradingUiState()
    object Empty : GradingUiState()
}

// Grading States
sealed class GradingState {
    object Idle : GradingState()
    data class InProgress(val examId: String, val examName: String) : GradingState()
    data class Success(val examId: String) : GradingState()
    data class Error(val examId: String, val message: String) : GradingState()
}

class GradingViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GradingUiState>(GradingUiState.Loading)
    val uiState: StateFlow<GradingUiState> = _uiState.asStateFlow()

    private val _gradingState = MutableStateFlow<GradingState>(GradingState.Idle)
    val gradingState: StateFlow<GradingState> = _gradingState.asStateFlow()

    private val _shouldLogout = MutableStateFlow(false)
    val shouldLogout: StateFlow<Boolean> = _shouldLogout.asStateFlow()

    init {
        loadOpenEndedExamsWithResponses()
    }

    fun loadOpenEndedExamsWithResponses() {
        viewModelScope.launch {
            _uiState.value = GradingUiState.Loading

            viewModelScope.launch {

                when (val result = adminRepository.getUngradedExams()) {
                    is NetworkResult.Success -> {
                        val examList = result.data

                        if (examList.isEmpty()) {
                            _uiState.value = GradingUiState.Empty
                            return@launch
                        }
                        _uiState.value = GradingUiState.Success(examList)

                    }

                    is NetworkResult.Error -> {
                        _uiState.value = GradingUiState.Error(
                            result.message
                        )
                    }

                    is NetworkResult.Loading -> {
                        // Keep current loading state
                    }
                }
            }

        }
    }

    fun startGrading(exam: UngradedExam) {
        viewModelScope.launch {
            _gradingState.value = GradingState.InProgress(
                examId = exam.examId,
                examName = exam.courseName
            )

            when (val result = adminRepository.gradeExam(GradeExamRequest(exam.examId))) {
                is NetworkResult.Success -> {
                    loadOpenEndedExamsWithResponses()
                    _gradingState.value = GradingState.Success(exam.examId)
                }

                is NetworkResult.Error -> {
                    _gradingState.value = GradingState.Error(
                        examId = exam.examId,
                        message = result.message
                    )
                }

                else -> {

                }
            }
        }
    }

    fun logout() {
        adminRepository.clearCurrentAdmin()
        _shouldLogout.value = true
    }

    fun onLogoutHandled() {
        _shouldLogout.value = false
    }
}