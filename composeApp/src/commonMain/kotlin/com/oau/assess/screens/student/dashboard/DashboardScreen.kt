import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.oau.assess.data.ExamAssignment
import com.oau.assess.screens.student.dashboard.DashboardUiState
import com.oau.assess.screens.student.dashboard.DashboardViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onExamClick: (studentId: String, examId: String, examTitle: String, examType: String, duration: Int) -> Unit,
    viewModel: DashboardViewModel = koinInject<DashboardViewModel>()
) {
    val student by viewModel.student.collectAsState()
    val shouldLogout by viewModel.shouldLogout.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val examAssignments by viewModel.examAssignments.collectAsState()

    // Define colors for exam cards
    val examColors = listOf(
        Color(0xFF81C784),
        Color(0xFFA5D6A7),
        Color(0xFF4FC3F7),
        Color(0xFFFFB74D),
        Color(0xFFE57373)
    )

    val primaryBlue = Color(0xFF2196F3)
    val lightGray = Color(0xFFF5F5F5)

    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
            delay(100)
            viewModel.onLogoutHandled()
        }
    }

    when (uiState) {
        is DashboardUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DashboardUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: ${(uiState as DashboardUiState.Error).message}",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                    Button(
                        onClick = { viewModel.retryLoadExams() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        is DashboardUiState.Empty -> {
            if (student == null) {
                viewModel.logout()
            } else {
                // Student loaded but no exams assigned
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(lightGray)
                ) {
                    // Top Navigation Bar
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏛️ OAU Assess",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                        },
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable {
                                            viewModel.logout()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                                        contentDescription = "Logout",
                                        modifier = Modifier.size(60.dp),
                                        tint = Color.Red
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Main Content with Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    ) {
                        // Welcome Message with Student Info
                        Text(
                            text = "Welcome, ${student!!.fullName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "Matric No: ${student!!.matricNo}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Empty State Content
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Empty state icon
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(
                                            Color(0xFFF0F0F0),
                                            RoundedCornerShape(60.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                                        contentDescription = "No exams",
                                        modifier = Modifier.size(60.dp),
                                        tint = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "No Exams Assigned",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                Text(
                                    text = "You don't have any exams assigned yet.\nPlease check back later or contact your instructor.",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.retryLoadExams() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryBlue
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Refresh",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is DashboardUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
            ) {
                // Top Navigation Bar
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏛️ OAU Assess",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    },
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        viewModel.logout()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(60.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    // Welcome Message with Student Info
                    Text(
                        text = "Welcome, ${student!!.fullName}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "Matric No: ${student!!.matricNo}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Here are the exams assigned to you. Click on an exam to start or continue.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Exams List with real data
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(
                            items = examAssignments,
                            key = { it.examId }
                        ) { assignment ->
                            val colorIndex = examAssignments.indexOf(assignment) % examColors.size
                            ExamAssignmentCard(
                                assignment = assignment,
                                backgroundColor = examColors[colorIndex],
                                onExamClick = {
                                    onExamClick(
                                        student!!.id,
                                        assignment.examId,
                                        assignment.courseName,
                                        assignment.examType,
                                        assignment.duration
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ExamAssignmentCard(
    assignment: ExamAssignment,
    backgroundColor: Color,
    onExamClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(
                enabled = false,
                onClick = { /* no-op when disabled */ }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = assignment.courseName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Course Code: ${assignment.courseCode}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Duration: ${assignment.duration} mins",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Type: ${if (assignment.examType == "McqQuestion") "Multiple-Choice" else "Theory"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = onExamClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(36.dp)
                ) {
                    Text(
                        text = "Start Exam",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right Visual Element
            Box(
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .background(
                        backgroundColor,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Exam visual representation
                Card(
                    modifier = Modifier.size(60.dp, 40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color.LightGray)
                            )
                        }
                    }
                }
            }
        }
    }
}