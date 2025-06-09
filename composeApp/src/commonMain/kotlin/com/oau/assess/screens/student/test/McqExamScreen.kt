package com.oau.assess.screens.student.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oau.assess.utils.formatTime
import kotlinx.coroutines.delay

data class McqOption(
    val id: String,
    val text: String
)

data class McqQuestion(
    val id: String,
    val question: String,
    val options: List<McqOption>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McqExamScreen(
    examTitle: String = "Biology Exam",
    totalDuration: Int = 30, // in minutes
    onExamComplete: (Map<String, String>) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var timeRemaining by remember { mutableStateOf(totalDuration * 60) } // in seconds

    // Sample questions - replace with actual data
    val questions = remember {
        listOf(
            McqQuestion(
                id = "q1",
                question = "What is the primary function of the mitochondria in a cell?",
                options = listOf(
                    McqOption("a", "Protein synthesis"),
                    McqOption("b", "Energy production"),
                    McqOption("c", "Waste disposal"),
                    McqOption("d", "Cell division")
                )
            ),
            // Add more questions here
        ).apply {
            // Generate 10 sample questions for demo
            val sampleQuestions = mutableListOf(this[0])
            repeat(9) { i ->
                sampleQuestions.add(
                    McqQuestion(
                        id = "q${i + 2}",
                        question = "Sample question ${i + 2}?",
                        options = listOf(
                            McqOption("a", "Option A"),
                            McqOption("b", "Option B"),
                            McqOption("c", "Option C"),
                            McqOption("d", "Option D")
                        )
                    )
                )
            }
        }
    }

    val currentQuestion = questions[currentQuestionIndex]
    val selectedOption = selectedAnswers[currentQuestion.id]

    // Timer effect
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        } else {
            // Time's up - submit exam
            onExamComplete(selectedAnswers)
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
                        option = option,
                        isSelected = selectedOption == option.id,
                        onSelect = {
                            selectedAnswers[currentQuestion.id] = option.id
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
                            onExamComplete(selectedAnswers)
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
                                    selectedAnswers.containsKey(question.id) -> Color(0xFF4CAF50)
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

@Composable
fun OptionCard(
    option: McqOption,
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
                text = option.text,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}