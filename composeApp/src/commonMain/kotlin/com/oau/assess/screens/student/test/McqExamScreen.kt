package com.oau.assess.screens.student.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import com.oau.assess.data.McqQuestion
import com.oau.assess.utils.ExamType
import com.oau.assess.utils.formatTime
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McqExamScreen(
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
    val mcqQuestions by viewModel.mcqQuestions.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val mcqAnswers by viewModel.mcqAnswers.collectAsState()

    // State management with examId key to prevent unnecessary resets
    var currentQuestionIndex by remember(examId) { mutableStateOf(0) }
    var timeRemaining by remember(examId) { mutableStateOf(totalDuration * 60) }
    var isTimerActive by remember(examId) { mutableStateOf(false) }

    // Exit confirmation dialog state
    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmissionDialog by remember { mutableStateOf(false) }

    val shouldLogout by viewModel.shouldLogout.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentStudent()
    }

    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
            delay(100)
            viewModel.onLogoutHandled()
        }
    }

    // Load questions when screen is first composed
    LaunchedEffect(examId) {
        viewModel.loadExamQuestions(examId)
    }

    // Separate timer activation from question loading
    LaunchedEffect(mcqQuestions.isNotEmpty()) {
        if (mcqQuestions.isNotEmpty() && !isTimerActive) {
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
            viewModel.submitMcqExam(ExamType.MCQ)
        }
    }

    // Handle submission success
    LaunchedEffect(submissionState) {
        when (submissionState) {
            is SubmissionUiState.Success -> {
                isTimerActive = false
                onExamComplete(mcqAnswers)
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
                    if (isTimerActive && mcqQuestions.isNotEmpty()) {
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
                if (mcqQuestions.isNotEmpty() && isTimerActive) {
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
                if (mcqQuestions.isEmpty()) {
                    EmptyMcqContent()
                } else {
                    ExamContent(
                        examTitle = examTitle,
                        questions = mcqQuestions,
                        currentQuestionIndex = currentQuestionIndex,
                        selectedAnswers = mcqAnswers,
                        onQuestionIndexChange = { index ->
                            currentQuestionIndex = index
                        },
                        onAnswerSelect = { questionId, optionText ->
                            viewModel.updateMcqAnswer(questionId, optionText)
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

    // Exit Confirmation Dialog
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirmExit = {
                showExitDialog = false
                isTimerActive = false // Stop the timer
                onNavigateBack()
            },
            onDismiss = {
                showExitDialog = false
            },
            answeredQuestions = viewModel.getAnsweredQuestionsCount(),
            totalQuestions = viewModel.getTotalMcqQuestionsCount()
        )
    }

    // Submission Confirmation Dialog
    if (showSubmissionDialog) {
        SubmissionConfirmationDialog(
            onConfirmSubmit = {
                showSubmissionDialog = false
                isTimerActive = false
                viewModel.submitMcqExam(ExamType.MCQ)
            },
            onDismiss = {
                showSubmissionDialog = false
            },
            answeredQuestions = viewModel.getAnsweredQuestionsCount(),
            totalQuestions = viewModel.getTotalMcqQuestionsCount()
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
                            viewModel.submitMcqExam(ExamType.MCQ) // Retry submission
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
private fun ExamContent(
    examTitle: String,
    questions: List<McqQuestion>,
    currentQuestionIndex: Int,
    selectedAnswers: Map<String, String>,
    onQuestionIndexChange: (Int) -> Unit,
    onAnswerSelect: (String, String) -> Unit,
    onExamSubmit: () -> Unit,
    submissionState: SubmissionUiState
) {
    val currentQuestion = questions[currentQuestionIndex]
    val selectedOption = selectedAnswers[currentQuestion.id]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

        // Question Text
        Text(
            text = currentQuestion.question,
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Options
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            currentQuestion.options.forEach { option ->
                OptionCard(
                    optionText = option,
                    isSelected = selectedOption == option,
                    onSelect = {
                        onAnswerSelect(currentQuestion.id, option)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                                selectedAnswers.containsKey(question.id) -> Color(0xFF4CAF50)
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
                text = "Exit Exam?",
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
                    text = "⚠️ Your progress will be lost and cannot be recovered.",
                    fontSize = 14.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Exit Exam",
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
private fun EmptyMcqContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No MCQ questions available",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "This assignment may contain only open-ended questions",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OptionCard(
    optionText: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(0xFF2196F3))
        } else {
            BorderStroke(1.dp, Color(0xFFE0E0E0))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF2196F3) else Color(0xFFBDBDBD),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFF2196F3),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = optionText,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}