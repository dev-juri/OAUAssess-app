package com.oau.assess.screens.admin.dashboard

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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

data class Exam(
    val name: String,
    val type: String,
    val courseCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit = {},
    onCreateExam: () -> Unit = {},
    viewModel: AdminDashboardViewModel = koinInject<AdminDashboardViewModel>()
) {

    val shouldLogout by viewModel.shouldLogout.collectAsState()
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

    val exams = remember {
        listOf(
            Exam("Midterm Exam", "Multiple Choice", "CS101"),
            Exam("Final Exam", "Open-ended", "CS101"),
            Exam("Quiz 1", "Multiple Choice", "MA102"),
            Exam("Lab Exam", "Open-ended", "EE201"),
            Exam("Practice Exam", "Multiple Choice", "PH101")
        )
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
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier.size(60.dp),
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
                    shape = RoundedCornerShape(8.dp)
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
                            text = "Actions",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF555555),
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    // Table Rows
                    LazyColumn {
                        items(exams) { exam ->
                            ExamRow(exam = exam, primaryBlue = primaryBlue)
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
fun ExamRow(exam: Exam, primaryBlue: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = exam.name,
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
                color = if (exam.type == "Multiple Choice") Color(0xFFE3F2FD) else Color(0xFFF3E5F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = exam.type,
                    color = if (exam.type == "Multiple Choice") primaryBlue else Color(0xFF7B1FA2),
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

        Box(
            modifier = Modifier.weight(1.5f),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = { /* Handle view/export */ },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = primaryBlue
                )
            ) {
                Text(
                    text = "View/Export Report",
                    fontSize = 12.sp
                )
            }
        }
    }
}