package com.oau.assess.screens.student.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.data.Question
import com.oau.assess.data.McqQuestion
import com.oau.assess.data.OeQuestion
import com.oau.assess.models.McqResponse
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

sealed class SubmissionUiState {
    object Idle : SubmissionUiState()
    object Loading : SubmissionUiState()
    data class Success(val message: String) : SubmissionUiState()
    data class Error(val message: String) : SubmissionUiState()
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

    // MCQ Submission States
    private val _submissionState = MutableStateFlow<SubmissionUiState>(SubmissionUiState.Idle)
    val submissionState: StateFlow<SubmissionUiState> = _submissionState.asStateFlow()

    // Store user's answers
    private val _mcqAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val mcqAnswers: StateFlow<Map<String, String>> = _mcqAnswers.asStateFlow()


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

    // MCQ Answer Management
    fun updateMcqAnswer(questionId: String, answer: String) {
        val currentAnswers = _mcqAnswers.value.toMutableMap()
        currentAnswers[questionId] = answer
        _mcqAnswers.value = currentAnswers
    }

    fun getMcqAnswer(questionId: String): String? {
        return _mcqAnswers.value[questionId]
    }

    // MCQ Submission
    fun submitMcqExam() {
        val currentStudent = _student.value
        val examId = _currentExamId.value

        if (currentStudent == null) {
            _submissionState.value = SubmissionUiState.Error("No student logged in")
            return
        }

        if (examId == null) {
            _submissionState.value = SubmissionUiState.Error("No exam selected")
            return
        }

        val answers = _mcqAnswers.value
        if (answers.isEmpty()) {
            _submissionState.value = SubmissionUiState.Error("Please answer at least one question")
            return
        }

        // Convert answers to McqResponse list
        val responses = answers.map { (questionId, answer) ->
            McqResponse(questionId = questionId, answer = answer)
        }

        submitMcqExam(examId, currentStudent.id, responses)
    }

    private fun submitMcqExam(
        examId: String,
        studentId: String,
        responses: List<McqResponse>
    ) {
        viewModelScope.launch {
            _submissionState.value = SubmissionUiState.Loading

            studentRepository.submitMcqExam(examId, studentId, responses)
                .onSuccess { response ->
                    if (response.success) {
                        _submissionState.value = SubmissionUiState.Success(response.message)
                        // Clear answers after successful submission
                        _mcqAnswers.value = emptyMap()
                    } else {
                        _submissionState.value =
                            SubmissionUiState.Error("Submission failed: ${response.message}")
                    }
                }
                .onFailure { exception ->
                    _submissionState.value = SubmissionUiState.Error(
                        exception.message ?: "An unexpected error occurred"
                    )
                }
        }
    }

    // Check if all MCQ questions are answered
    fun areAllMcqQuestionsAnswered(): Boolean {
        val mcqQuestionIds = _mcqQuestions.value.map { it.id }
        val answeredQuestionIds = _mcqAnswers.value.keys
        return mcqQuestionIds.all { it in answeredQuestionIds }
    }

    // Get answered questions count
    fun getAnsweredQuestionsCount(): Int {
        return _mcqAnswers.value.size
    }

    // Get total MCQ questions count
    fun getTotalMcqQuestionsCount(): Int {
        return _mcqQuestions.value.size
    }

    fun resetSubmissionState() {
        _submissionState.value = SubmissionUiState.Idle
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
        _mcqAnswers.value = emptyMap()
        _submissionState.value = SubmissionUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        clearData()
    }
}