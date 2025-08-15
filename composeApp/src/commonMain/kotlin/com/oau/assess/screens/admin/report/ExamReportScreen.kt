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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.oau.assess.models.Student
import com.oau.assess.screens.admin.report.ExamReportViewModel
import com.oau.assess.utils.ScreenExamType
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamReportScreen(
    examId: String,
    examType: String,
    onBackPressed: () -> Unit,
    viewModel: ExamReportViewModel = koinInject<ExamReportViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(examId) {
        viewModel.loadExamReport(examId)
    }

    // Handle download success/error messages
    LaunchedEffect(uiState.reportDownloadSuccess) {
        if (uiState.reportDownloadSuccess) {
            viewModel.clearDownloadSuccess()
        }
    }

    LaunchedEffect(uiState.scriptsDownloadSuccess) {
        if (uiState.scriptsDownloadSuccess) {
            viewModel.clearDownloadSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Exam Report",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }, navigationIcon = {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Show download error if any
        if (uiState.downloadError != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Download Error: ${uiState.downloadError}",
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearDownloadError() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }

        // Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "View and download the report of the exam",
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D)
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0xFFECF0F1))

                Spacer(modifier = Modifier.height(16.dp))

                // Course Information
                Text(
                    text = "Course Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CourseInfoRow(label = "Course Name:", value = uiState.examTitle)
                CourseInfoRow(
                    label = "Exam Type:",
                    value = if (examType == ScreenExamType.McqQuestion.name) ScreenExamType.McqQuestion.examType else ScreenExamType.OeQuestion.examType
                )
            }
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF3498DB)
                    )
                }
            }

            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE74C3C)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.error!!,
                            fontSize = 14.sp,
                            color = Color(0xFF7F8C8D),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.retryLoading() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3498DB)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                // Students List
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Students",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2C3E50)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Name",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3E50),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Matriculation Number",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3E50),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Score",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3E50),
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(
                            Modifier, DividerDefaults.Thickness, color = Color(0xFFECF0F1)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.students) { student ->
                                StudentRow(student = student)
                            }
                        }
                    }
                }

                // Download Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.downloadExamReport(examId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF27AE60)
                        ),
                        enabled = !uiState.isDownloadingReport
                    ) {
                        if (uiState.isDownloadingReport) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF27AE60),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Download Score Sheet (Excel)")
                        }
                    }

                    OutlinedButton(
                        enabled = examType == ScreenExamType.OeQuestion.name && !uiState.isDownloadingScripts,
                        onClick = { viewModel.downloadExamScripts(examId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF3498DB)
                        )
                    ) {
                        if (uiState.isDownloadingScripts) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF3498DB),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Download Responses (Zip)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseInfoRow(
    label: String, value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF7F8C8D),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF2C3E50),
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun StudentRow(
    student: Student
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = student.studentName,
            fontSize = 14.sp,
            color = Color(0xFF2C3E50),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = student.matricNumber,
            fontSize = 14.sp,
            color = Color(0xFF3498DB),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = student.score.toString(),
            fontSize = 14.sp,
            color = Color(0xFF27AE60),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End
        )
    }
}