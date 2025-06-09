package com.oau.assess.screens.student.test

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oau.assess.utils.formatTime
import kotlinx.coroutines.delay

data class OpenEndedQuestion(
    val id: String,
    val question: String,
    val wordLimit: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenEndedExamScreen(
    examTitle: String = "Environmental Science Exam",
    totalDuration: Int = 30, // in minutes
    onSubmit: (Map<String, String>) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var timeRemaining by remember { mutableStateOf(totalDuration * 60) } // in seconds

    // Sample questions - replace with actual data
    val questions = remember {
        listOf(
            OpenEndedQuestion(
                id = "q1",
                question = "Explain the concept of 'sustainable development' and provide three examples of sustainable practices in urban planning."
            ),
            OpenEndedQuestion(
                id = "q2",
                question = "Discuss the impact of climate change on biodiversity and suggest mitigation strategies."
            ),
            OpenEndedQuestion(
                id = "q3",
                question = "Analyze the role of renewable energy in reducing carbon emissions."
            )
        )
    }

    val currentQuestion = questions[currentQuestionIndex]
    var currentAnswer by remember(currentQuestion.id) {
        mutableStateOf(answers[currentQuestion.id] ?: "")
    }

    // Timer effect
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        } else {
            // Time's up - submit exam
            onSubmit(answers)
        }
    }

    // Save answer when it changes
    LaunchedEffect(currentAnswer) {
        answers[currentQuestion.id] = currentAnswer
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
            actions = {
                // Timer Display
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
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Main Content
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

                // Answer Text Field
                OutlinedTextField(
                    value = currentAnswer,
                    onValueChange = { currentAnswer = it },
                    placeholder = {
                        Text(
                            text = "Type your answer here...",
                            color = Color.Gray
                        )
                    },
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
                            currentQuestionIndex--
                        }
                    },
                    enabled = currentQuestionIndex > 0,
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
                            currentQuestionIndex++
                        } else {
                            // Last question - submit exam
                            onSubmit(answers)
                        }
                    },
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
                            .clickable { currentQuestionIndex = index }
                    )
                }
            }
        }
    }
}