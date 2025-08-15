package com.oau.assess.screens.admin.grading

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.oau.assess.models.UngradedExam
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onGradeExam: (UngradedExam) -> Unit = {},
    viewModel: GradingViewModel = koinInject<GradingViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldLogout by viewModel.shouldLogout.collectAsState()
    val gradingState by viewModel.gradingState.collectAsState()
    val primaryBlue = Color(0xFF2196F3)

    LaunchedEffect(Unit) {
        viewModel.loadOpenEndedExamsWithResponses()
    }

    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
            viewModel.onLogoutHandled()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person, // Placeholder for OAU logo
                            contentDescription = "OAU Logo",
                            modifier = Modifier.size(24.dp),
                            tint = primaryBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OAU Assess - Grading",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOpenEndedExamsWithResponses() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(24.dp),
                            tint = primaryBlue
                        )
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Main Content
            when (val state = uiState) {
                is GradingUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                }

                is GradingUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadOpenEndedExamsWithResponses() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is GradingUiState.Success -> {
                    ExamGradingListContent(
                        exams = state.exams,
                        primaryBlue = primaryBlue,
                        onGradeExam = { exam ->
                            viewModel.startGrading(exam)
                            onGradeExam(exam)
                        },
                        gradingState = gradingState
                    )
                }

                is GradingUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No open-ended exams with responses available for grading",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadOpenEndedExamsWithResponses() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }
        }

        // Grading Overlay
        if (gradingState is GradingState.InProgress) {
            GradingOverlay(
                examName = (gradingState as GradingState.InProgress).examName,
                primaryBlue = primaryBlue,
                modifier = Modifier.zIndex(1f)
            )
        }
    }
}

@Composable
private fun ExamGradingListContent(
    exams: List<UngradedExam>,
    primaryBlue: Color,
    onGradeExam: (UngradedExam) -> Unit,
    gradingState: GradingState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Open-Ended Exams Ready for Grading",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Exams Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Exam Name",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = "Course Code",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Actions",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Table Rows
                if (exams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exams available for grading",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn {
                        items(exams) { exam ->
                            ExamGradingRow(
                                exam = exam,
                                primaryBlue = primaryBlue,
                                onGradeExam = onGradeExam,
                                isGradingInProgress = gradingState is GradingState.InProgress,
                                isCurrentlyGrading = gradingState is GradingState.InProgress &&
                                        gradingState.examId == exam.examId
                            )
                            if (exam != exams.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0xFFE0E0E0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamGradingRow(
    exam: UngradedExam,
    primaryBlue: Color,
    onGradeExam: (UngradedExam) -> Unit,
    isGradingInProgress: Boolean,
    isCurrentlyGrading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = exam.courseName,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f)
        )

        Text(
            text = exam.courseCode,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Grade Button
        Box(
            modifier = Modifier.weight(1.5f),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrentlyGrading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = primaryBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grading...",
                        fontSize = 12.sp,
                        color = primaryBlue
                    )
                }
            } else {
                Button(
                    onClick = { onGradeExam(exam) },
                    enabled = !isGradingInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGradingInProgress) Color.Gray else primaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Grade",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GradingOverlay(
    examName: String,
    primaryBlue: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = primaryBlue,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Grading in Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Grading responses for:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = examName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please wait while we process all responses...",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}