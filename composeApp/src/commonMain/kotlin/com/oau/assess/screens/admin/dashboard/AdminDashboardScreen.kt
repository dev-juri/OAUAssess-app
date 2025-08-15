package com.oau.assess.screens.admin.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.oau.assess.models.Exam
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit = {},
    onCreateExam: () -> Unit = {},
    onUpdateExam: (Exam) -> Unit = {},
    onViewReport: (Exam) -> Unit = {},
    onNavigateToGrade: () -> Unit = {},
    viewModel: AdminDashboardViewModel = koinInject<AdminDashboardViewModel>()
) {

    val uiState by viewModel.uiState.collectAsState()
    val shouldLogout by viewModel.shouldLogout.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val primaryBlue = Color(0xFF2196F3)

    LaunchedEffect(Unit) {
        viewModel.loadLoggedInAdmin()
    }

    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
            delay(100)
            viewModel.onLogoutHandled()
        }
    }

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
                        text = "OAU Assess",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            },
            actions = {
                Text(
                    text = "Grade",
                    modifier = Modifier.clickable { onNavigateToGrade() },
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(onClick = { viewModel.refreshExams() }) {
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
            is AdminDashboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            }

            is AdminDashboardUiState.Error -> {
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
                        onClick = { viewModel.refreshExams() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text("Retry")
                    }
                }
            }

            is AdminDashboardUiState.Success -> {
                ExamListContent(
                    exams = state.exams,
                    primaryBlue = primaryBlue,
                    onCreateExam = onCreateExam,
                    onUpdateExam = onUpdateExam,
                    onViewReport = onViewReport,
                    isLoading = isLoading
                )
            }

            is AdminDashboardUiState.Empty -> {
                onLogout()
                viewModel.logout()
            }
        }
    }
}

@Composable
private fun ExamListContent(
    exams: List<Exam>,
    primaryBlue: Color,
    onCreateExam: () -> Unit,
    onUpdateExam: (Exam) -> Unit,
    onViewReport: (Exam) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with New Exam button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exams",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Button(
                onClick = onCreateExam,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Exam")
            }
        }

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
                        text = "Exam Type",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Course Code",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Duration",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Actions",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(2f), // Made wider to accommodate both buttons
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Loading or Exams Content
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                } else if (exams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exams found",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Table Rows
                    LazyColumn {
                        items(exams) { exam ->
                            ExamRow(
                                exam = exam,
                                primaryBlue = primaryBlue,
                                onUpdateExam = onUpdateExam,
                                onViewReport = onViewReport
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
fun ExamRow(
    exam: Exam,
    primaryBlue: Color,
    onUpdateExam: (Exam) -> Unit,
    onViewReport: (Exam) -> Unit
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

        // Exam Type with colored background
        Box(
            modifier = Modifier
                .weight(1.5f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = if (exam.examType == "McqQuestion") Color(0xFFE3F2FD) else Color(0xFFF3E5F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = if (exam.examType == "McqQuestion") "Multiple Choice" else "Open-Ended",
                    color = if (exam.examType == "McqQuestion") primaryBlue else Color(0xFF7B1FA2),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = exam.courseCode,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "${exam.duration} min",
            color = Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )

        // Actions Column
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View/Export Button
            TextButton(
                onClick = { onViewReport(exam) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = primaryBlue
                )
            ) {
                Text(
                    text = "View/Export",
                    fontSize = 12.sp
                )
            }

            // Upload Questions Button (only show if no questions)
            if (exam.questions.isEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { onUpdateExam(exam) }, // Call the callback with the exam
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = primaryBlue
                    )
                ) {
                    Text(
                        text = "Upload Questions",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}