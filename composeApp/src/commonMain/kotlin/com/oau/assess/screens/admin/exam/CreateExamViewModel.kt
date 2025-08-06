package com.oau.assess.screens.admin.exam

import androidx.lifecycle.ViewModel
import com.oau.assess.repositories.admin.AdminRepository
import androidx.lifecycle.viewModelScope
import com.oau.assess.models.CreateExamResponse
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.w3c.files.File

class CreateExamViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // UI State for admin authentication
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // UI State for exam creation
    private val _examCreationState = MutableStateFlow<ExamCreationState>(ExamCreationState.Idle)
    val examCreationState: StateFlow<ExamCreationState> = _examCreationState.asStateFlow()

    // UI State for form validation
    private val _formErrors = MutableStateFlow<FormErrors>(FormErrors())
    val formErrors: StateFlow<FormErrors> = _formErrors.asStateFlow()

    init {
        checkAdminLoginStatus()
    }

    /**
     * Checks if an admin is currently logged in
     */
    fun checkAdminLoginStatus() {
        val admin = adminRepository.getAdmin()
        _isAdminLoggedIn.value = !admin.isNullOrEmpty()
    }

    /**
     * Creates an exam with the provided parameters
     */
    fun createExam(
        courseName: String,
        courseCode: String,
        duration: String,
        questionCount: String,
        examType: String,
        tutorialListFile: File?
    ) {
        // Validate inputs first
        val validationErrors = validateExamForm(
            courseName, courseCode, duration, questionCount, examType, tutorialListFile
        )

        if (validationErrors.hasErrors()) {
            _formErrors.value = validationErrors
            return
        }

        // Clear any previous errors
        _formErrors.value = FormErrors()

        // Check if admin is logged in before proceeding
        if (!_isAdminLoggedIn.value) {
            _examCreationState.value = ExamCreationState.Error("Admin not logged in")
            return
        }

        viewModelScope.launch {
            _examCreationState.value = ExamCreationState.Loading

            try {
                val result = adminRepository.createExam(
                    courseName = courseName.trim(),
                    courseCode = courseCode.trim().uppercase(),
                    duration = duration.toInt(),
                    questionCount = questionCount.toInt(),
                    examType = examType,
                    tutorialListFile = tutorialListFile!!
                )

                _examCreationState.value = when (result) {
                    is NetworkResult.Success -> ExamCreationState.Success(result.data)
                    is NetworkResult.Error -> ExamCreationState.Error(result.message)
                    NetworkResult.Loading -> ExamCreationState.Loading
                }
            } catch (e: Exception) {
                _examCreationState.value = ExamCreationState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Validates the exam creation form
     */
    private fun validateExamForm(
        courseName: String,
        courseCode: String,
        duration: String,
        questionCount: String,
        examType: String,
        tutorialListFile: File?
    ): FormErrors {
        val errors = FormErrors()

        if (courseName.isBlank()) {
            errors.courseName = "Course name is required"
        }

        if (courseCode.isBlank()) {
            errors.courseCode = "Course code is required"
        }

        if (duration.isBlank()) {
            errors.duration = "Duration is required"
        } else {
            try {
                val durationInt = duration.toInt()
                if (durationInt <= 0) {
                    errors.duration = "Duration must be greater than 0"
                }
            } catch (e: NumberFormatException) {
                errors.duration = "Duration must be a valid number"
            }
        }

        if (questionCount.isBlank()) {
            errors.questionCount = "Question count is required"
        } else {
            try {
                val questionCountInt = questionCount.toInt()
                if (questionCountInt <= 0) {
                    errors.questionCount = "Question count must be greater than 0"
                }
            } catch (e: NumberFormatException) {
                errors.questionCount = "Question count must be a valid number"
            }
        }

        if (examType.isBlank()) {
            errors.examType = "Exam type is required"
        }

        if (tutorialListFile == null) {
            errors.tutorialListFile = "Tutorial list file is required"
        }
        return errors
    }

    /**
     * Resets the exam creation state to idle
     */
    fun resetExamCreationState() {
        _examCreationState.value = ExamCreationState.Idle
    }

    /**
     * Clears form errors
     */
    fun clearFormErrors() {
        _formErrors.value = FormErrors()
    }

    /**
     * Logs out the current admin
     */
    fun logoutAdmin() {
        adminRepository.clearCurrentAdmin()
        _isAdminLoggedIn.value = false
        resetExamCreationState()
    }

    /**
     * Represents the different states of exam creation
     */
    sealed class ExamCreationState {
        object Idle : ExamCreationState()
        object Loading : ExamCreationState()
        data class Success(val response: CreateExamResponse) : ExamCreationState()
        data class Error(val message: String) : ExamCreationState()
    }

    /**
     * Represents form validation errors
     */
    data class FormErrors(
        var courseName: String? = null,
        var courseCode: String? = null,
        var duration: String? = null,
        var questionCount: String? = null,
        var examType: String? = null,
        var tutorialListFile: String? = null
    ) {
        fun hasErrors(): Boolean {
            return courseName != null || courseCode != null || duration != null ||
                    questionCount != null || examType != null || tutorialListFile != null
        }
    }
}