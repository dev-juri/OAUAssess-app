package com.oau.assess.screens.admin.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Exam(
    val name: String,
    val type: String,
    val courseCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit = {}
) {
    val primaryBlue = Color(0xFF2196F3)
    val lightGray = Color(0xFFF5F5F5)

    val exams = remember {
        listOf(
            Exam("Midterm Exam", "Multiple Choice", "CS101"),
            Exam("Final Exam", "Open-ended", "CS101"),
            Exam("Quiz 1", "Multiple Choice", "MA102"),
            Exam("Lab Exam", "Open-ended", "EE201"),
            Exam("Practice Exam", "Multiple Choice", "PH101")
        )
    }

    var selectedTab by remember { mutableStateOf("Exams") }

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
                IconButton(onClick = { /* Handle notifications */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.Gray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Dashboard", "Exams", "Courses", "Students").forEach { tab ->
                TextButton(
                    onClick = { selectedTab = tab },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tab,
                        color = if (tab == selectedTab) primaryBlue else Color.Gray,
                        fontWeight = if (tab == selectedTab) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

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
                    onClick = { /* Handle new exam */ },
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