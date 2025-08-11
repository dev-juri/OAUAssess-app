package com.oau.assess.screens.student.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oau.assess.data.OeQuestion
import com.oau.assess.utils.ExamType
import com.oau.assess.utils.formatTime
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenEndedExamScreen(
    onLogout: () -> Unit,
    examId: String,
    examTitle: String,
    totalDuration: Int,
    viewModel: ExamViewModel = koinInject<ExamViewModel>(),
    onExamComplete: (Map<String, String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val student by viewModel.student.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val oeQuestions by viewModel.oeQuestions.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val oeAnswers by viewModel.oeAnswers.collectAsState()

    // State management with examId key to prevent unnecessary resets
    var currentQuestionIndex by remember(examId) { mutableStateOf(0) }
    var timeRemaining by remember(examId) { mutableStateOf(totalDuration * 60) }
    var isTimerActive by remember(examId) { mutableStateOf(false) }

    // Dialog states
    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmissionDialog by remember { mutableStateOf(false) }
    val shouldLogout by viewModel.shouldLogout.collectAsState()

    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
            delay(100)
            viewModel.onLogoutHandled()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentStudent()
    }

    // Load questions when screen is first composed
    LaunchedEffect(examId) {
        viewModel.loadExamQuestions(examId)
    }

    // Separate timer activation from question loading
    LaunchedEffect(oeQuestions.isNotEmpty()) {
        if (oeQuestions.isNotEmpty() && !isTimerActive) {
            isTimerActive = true
        }
    }

    // Improved timer logic with proper key and conditions
    LaunchedEffect(isTimerActive, timeRemaining) {
        if (isTimerActive && timeRemaining > 0) {
            delay(1000)
            timeRemaining = (timeRemaining - 1).coerceAtLeast(0)
        } else if (timeRemaining <= 0 && isTimerActive) {
            // Time's up - submit exam automatically
            isTimerActive = false
            viewModel.submitMcqExam(ExamType.OE)
        }
    }

    // Handle submission success
    LaunchedEffect(submissionState) {
        when (submissionState) {
            is SubmissionUiState.Success -> {
                isTimerActive = false
                onExamComplete(oeAnswers)
            }

            else -> {}
        }
    }

    // Clear data when navigating away
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "🏛️ OAU Assess",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    // Show exit confirmation if exam is active, otherwise navigate back directly
                    if (isTimerActive && oeQuestions.isNotEmpty()) {
                        showExitDialog = true
                    } else {
                        onNavigateBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Timer Display - only show when questions are loaded and timer is active
                if (oeQuestions.isNotEmpty() && isTimerActive) {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time remaining",
                            tint = if (timeRemaining < 300) Color.Red else Color(0xFF666666)
                        )
                        Text(
                            text = formatTime(timeRemaining),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (timeRemaining < 300) Color.Red else Color.Black
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Main Content
        when (uiState) {
            is ExamUiState.Loading -> {
                LoadingContent()
            }

            is ExamUiState.Success -> {
                if (oeQuestions.isEmpty()) {
                    EmptyOeContent()
                } else {
                    OpenEndedContent(
                        examTitle = examTitle,
                        questions = oeQuestions,
                        currentQuestionIndex = currentQuestionIndex,
                        answers = oeAnswers,
                        onQuestionIndexChange = { index ->
                            currentQuestionIndex = index
                        },
                        onAnswerChange = { questionId, answer ->
                            viewModel.updateOeAnswer(questionId, answer)
                        },
                        onExamSubmit = {
                            showSubmissionDialog = true
                        },
                        submissionState = submissionState
                    )
                }
            }

            is ExamUiState.Error -> {
                ErrorContent(
                    message = (uiState as ExamUiState.Error).message,
                    onRetry = { viewModel.retryLoadQuestions() }
                )
            }

            is ExamUiState.Empty -> {
                if (student == null) {
                    viewModel.logout()
                } else {
                    EmptyContent()
                }
            }
        }
    }

    // Exit Confirmation Dialog - with automatic submission
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirmExit = {
                showExitDialog = false
                isTimerActive = false
                // Submit exam before exiting
                viewModel.submitMcqExam(ExamType.OE)
                // Note: Navigation will happen via submissionState success handler
            },
            onDismiss = {
                showExitDialog = false
            },
            answeredQuestions = viewModel.getAnsweredOeQuestionsCount(),
            totalQuestions = viewModel.getTotalOeQuestionsCount()
        )
    }

    // Submission Confirmation Dialog
    if (showSubmissionDialog) {
        SubmissionConfirmationDialog(
            onConfirmSubmit = {
                showSubmissionDialog = false
                isTimerActive = false
                viewModel.submitMcqExam(ExamType.OE)
            },
            onDismiss = {
                showSubmissionDialog = false
            },
            answeredQuestions = viewModel.getAnsweredOeQuestionsCount(),
            totalQuestions = viewModel.getTotalOeQuestionsCount()
        )
    }

    // Submission Loading/Error Dialog
    when (submissionState) {
        is SubmissionUiState.Loading -> {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = "Submitting Exam",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF2196F3)
                        )
                        Text("Please wait while we submit your exam...")
                    }
                },
                confirmButton = { },
                containerColor = Color.White
            )
        }

        is SubmissionUiState.Error -> {
            AlertDialog(
                onDismissRequest = {
                    viewModel.resetSubmissionState()
                },
                title = {
                    Text(
                        text = "Submission Failed",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                },
                text = {
                    Text((submissionState as SubmissionUiState.Error).message)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetSubmissionState()
                            viewModel.submitMcqExam(ExamType.OE) // Retry submission
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Retry")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetSubmissionState()
                        }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White
            )
        }

        else -> {}
    }
}

@Composable
private fun OpenEndedContent(
    examTitle: String,
    questions: List<OeQuestion>,
    currentQuestionIndex: Int,
    answers: Map<String, String>,
    onQuestionIndexChange: (Int) -> Unit,
    onAnswerChange: (String, String) -> Unit,
    onExamSubmit: () -> Unit,
    submissionState: SubmissionUiState
) {
    val currentQuestion = questions[currentQuestionIndex]
    var currentAnswer by remember(currentQuestion.id) {
        mutableStateOf(answers[currentQuestion.id] ?: "")
    }

    // Save answer when it changes
    LaunchedEffect(currentAnswer) {
        onAnswerChange(currentQuestion.id, currentAnswer)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        // Exam Title and Progress
        Text(
            text = examTitle,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Question Counter
        Text(
            text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Progress Indicator
        LinearProgressIndicator(
            progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(bottom = 32.dp),
            color = Color(0xFF2196F3),
            trackColor = Color(0xFFE0E0E0),
        )

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Question Text
            Text(
                text = currentQuestion.question,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp),
                lineHeight = 26.sp
            )


            OutlinedTextField(
                value = currentAnswer,
                onValueChange = { newValue ->
                    if (newValue.length <= 2000) {
                        currentAnswer = newValue
                    }
                },
                placeholder = {
                    Text(
                        text = "Type your answer here...",
                        color = Color.Gray
                    )
                },
                enabled = submissionState !is SubmissionUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )

            Text(
                text = "${currentAnswer.length}/2000 characters",
                fontSize = 14.sp,
                color = if (currentAnswer.length > 2400) Color.Red else Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (currentQuestionIndex > 0) {
                        onQuestionIndexChange(currentQuestionIndex - 1)
                    }
                },
                enabled = currentQuestionIndex > 0 && submissionState !is SubmissionUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Previous",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    if (currentQuestionIndex < questions.size - 1) {
                        onQuestionIndexChange(currentQuestionIndex + 1)
                    } else {
                        // Last question - show submission dialog
                        onExamSubmit()
                    }
                },
                enabled = submissionState !is SubmissionUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = if (currentQuestionIndex == questions.size - 1) "Submit" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Question Navigation Dots
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            questions.forEachIndexed { index, question ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .background(
                            color = when {
                                index == currentQuestionIndex -> Color(0xFF2196F3)
                                answers[question.id]?.isNotBlank() == true -> Color(0xFF4CAF50)
                                else -> Color(0xFFE0E0E0)
                            },
                            shape = CircleShape
                        )
                        .clickable {
                            if (submissionState !is SubmissionUiState.Loading) {
                                onQuestionIndexChange(index)
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun SubmissionConfirmationDialog(
    onConfirmSubmit: () -> Unit,
    onDismiss: () -> Unit,
    answeredQuestions: Int,
    totalQuestions: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Submit Exam?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to submit your exam?",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Progress: $answeredQuestions of $totalQuestions questions answered",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (answeredQuestions < totalQuestions) {
                    Text(
                        text = "⚠️ You have ${totalQuestions - answeredQuestions} unanswered questions.",
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit Exam",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color(0xFF2196F3)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Continue Exam",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
    answeredQuestions: Int,
    totalQuestions: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color(0xFFFF9800)
            )
        },
        title = {
            Text(
                text = "Exit and Submit Exam?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to exit the exam?",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Progress: $answeredQuestions of $totalQuestions questions answered",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "📝 Your exam will be automatically submitted before exiting.",
                    fontSize = 14.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit & Exit",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color(0xFF2196F3)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Continue Exam",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3)
            )
            Text(
                text = "Loading exam questions...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error loading questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No questions available",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyOeContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No Open-Ended questions available",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "This assignment may contain only multiple choice questions",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}