package com.oau.assess.screens.admin.exam

import androidx.compose.runtime.Composable
import com.oau.assess.models.Exam
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateOeExamScreen(
    exam: Exam,
    onBackPressed: () -> Unit,
    onQuestionsFileSelected: (ByteArray, String) -> Unit,
    onAnswerKeyFileSelected: (ByteArray, String) -> Unit,
    onUpdateExam: (ByteArray?, String?, ByteArray?, String?) -> Unit
) {
    var selectedQuestionsFile by remember { mutableStateOf<Pair<ByteArray, String>?>(null) }
    var selectedAnswerKeyFile by remember { mutableStateOf<Pair<ByteArray, String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OAU Assess", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { }) { Text("Dashboard") }
                        TextButton(onClick = { }) { Text("Exams") }
                        TextButton(onClick = { }) { Text("Assessments") }
                        TextButton(onClick = { }) { Text("Results") }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Update Open-Ended Exam",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Exam Title
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Exam Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = exam.courseName,
                    onValueChange = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Course Code
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Course Code",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = exam.courseCode,
                    onValueChange = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Duration
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Duration (minutes)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = exam.duration.toString(),
                    onValueChange = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Exam Type
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Exam Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = exam.examType,
                    onValueChange = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Upload Questions
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Upload Questions (PDF/DOCX)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        // File picker logic here
                        // onQuestionsFileSelected will be called when file is selected
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF007BFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = selectedQuestionsFile?.second ?: "Choose File",
                        fontSize = 16.sp
                    )
                }
            }

            // Upload Answer Key
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Upload Answer Key (PDF/DOCX)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        // File picker logic here
                        // onAnswerKeyFileSelected will be called when file is selected
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF007BFF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = selectedAnswerKeyFile?.second ?: "Choose File",
                        fontSize = 16.sp
                    )
                }
            }

            // Update Button
            Button(
                onClick = {
                    isLoading = true
                    onUpdateExam(
                        selectedQuestionsFile?.first,
                        selectedQuestionsFile?.second,
                        selectedAnswerKeyFile?.first,
                        selectedAnswerKeyFile?.second
                    )
                },
                enabled = (selectedQuestionsFile != null || selectedAnswerKeyFile != null) && !isLoading,
                modifier = Modifier
                    .align(Alignment.End)
                    .height(48.dp)
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007BFF)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Update Exam",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}