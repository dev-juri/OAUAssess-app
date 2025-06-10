package com.oau.assess.screens.student.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.data.Question
import com.oau.assess.data.McqQuestion
import com.oau.assess.data.OeQuestion
import com.oau.assess.models.StudentData
import com.oau.assess.repositories.StudentRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class ExamUiState {
    object Loading : ExamUiState()
    data class Success(val questions: List<Question>) : ExamUiState()
    data class Error(val message: String) : ExamUiState()
    object Empty : ExamUiState()
}

class ExamViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private val _student = MutableStateFlow<StudentData?>(null)
    val student: StateFlow<StudentData?> = _student.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _mcqQuestions = MutableStateFlow<List<McqQuestion>>(emptyList())
    val mcqQuestions: StateFlow<List<McqQuestion>> = _mcqQuestions.asStateFlow()

    private val _oeQuestions = MutableStateFlow<List<OeQuestion>>(emptyList())
    val oeQuestions: StateFlow<List<OeQuestion>> = _oeQuestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentExamId = MutableStateFlow<String?>(null)
    val currentExamId: StateFlow<String?> = _currentExamId.asStateFlow()

    init {
        loadCurrentStudent()
    }

    private fun loadCurrentStudent() {
        val currentStudent = studentRepository.getCurrentStudent()
        _student.value = currentStudent

        if (currentStudent == null) {
            _uiState.value = ExamUiState.Empty
        }
    }

    fun loadExamQuestions(examId: String) {
        val currentStudent = _student.value
        if (currentStudent == null) {
            _uiState.value = ExamUiState.Error("No student logged in")
            return
        }

        _currentExamId.value = examId
        loadQuestions(currentStudent.id, examId)
    }

    private fun loadQuestions(studentId: String, examId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ExamUiState.Loading

            when (val result = studentRepository.getAssignmentQuestions(studentId, examId)) {
                is NetworkResult.Success -> {
                    _questions.value = result.data
                    separateQuestionTypes(result.data)

                    _uiState.value = if (result.data.isEmpty()) {
                        ExamUiState.Empty
                    } else {
                        ExamUiState.Success(result.data)
                    }
                    _isLoading.value = false
                }
                is NetworkResult.Error -> {
                    _uiState.value = ExamUiState.Error(result.message)
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    private fun separateQuestionTypes(questions: List<Question>) {
        _mcqQuestions.value = questions.filterIsInstance<McqQuestion>()
        _oeQuestions.value = questions.filterIsInstance<OeQuestion>()
    }

    fun retryLoadQuestions() {
        val studentId = _student.value?.id
        val examId = _currentExamId.value

        if (studentId != null && examId != null) {
            loadQuestions(studentId, examId)
        }
    }

    fun clearData() {
        _questions.value = emptyList()
        _mcqQuestions.value = emptyList()
        _oeQuestions.value = emptyList()
        _currentExamId.value = null
        _uiState.value = ExamUiState.Loading
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        clearData()
    }
}