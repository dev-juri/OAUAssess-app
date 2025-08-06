package com.oau.assess.screens.admin.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.w3c.files.File
import org.w3c.dom.HTMLInputElement
import kotlinx.browser.document
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    viewModel: CreateExamViewModel = koinInject<CreateExamViewModel>(),
    onNavigateToLogin: () -> Unit = {},
    onExamCreated: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Observe ViewModel states
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val examCreationState by viewModel.examCreationState.collectAsState()
    val formErrors by viewModel.formErrors.collectAsState()

    // Form state
    var examName by remember { mutableStateOf("") }
    var selectedExamType by remember { mutableStateOf("") }
    var numberOfQuestions by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Define colors matching DashboardScreen
    val primaryBlue = Color(0xFF2196F3)
    val lightGray = Color(0xFFF5F5F5)

    val examTypes = listOf("Multiple Choice", "Open-ended")

    // Handle navigation to login if not authenticated
    LaunchedEffect(isAdminLoggedIn) {
        if (!isAdminLoggedIn) {
            onNavigateToLogin()
        }
    }

    // Handle exam creation success
    LaunchedEffect(examCreationState) {
        if (examCreationState is CreateExamViewModel.ExamCreationState.Success) {
            onExamCreated()
            viewModel.resetExamCreationState()
        }
    }

    val handleFileSelection = remember {
        {
            try {
                val input = document.createElement("input")
                    .unsafeCast<HTMLInputElement>()

                input.type = "file"
                input.accept = ".csv,.xlsx,.xls"

                input.onchange = { event ->
                    val target = event.target as HTMLInputElement
                    val file = target.files?.item(0)

                    if (file != null) {
                        selectedFile = file
                        if (formErrors.tutorialListFile != null) {
                            viewModel.clearFormErrors()
                        }
                    }
                }

                input.click()
            } catch (_: Exception) {

            }
        }
    }


    when (examCreationState) {
        is CreateExamViewModel.ExamCreationState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryBlue)
            }
        }

        is CreateExamViewModel.ExamCreationState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: ${(examCreationState as CreateExamViewModel.ExamCreationState.Error).message}",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                    Button(
                        onClick = { viewModel.resetExamCreationState() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue
                        )
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightGray)
            ) {
                // Top Navigation Bar - matching DashboardScreen style
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
                                        viewModel.logoutAdmin()
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

                // Main Content - matching DashboardScreen padding and structure
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    // Title - matching DashboardScreen style
                    Text(
                        text = "Create New Exam",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "Fill in the details below to create a new exam for students.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Form Fields in Cards - similar to ExamAssignmentCard styling
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Exam Name Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Exam Name",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = examName,
                                    onValueChange = {
                                        examName = it
                                        if (formErrors.courseName != null) {
                                            viewModel.clearFormErrors()
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Enter exam name",
                                            color = Color(0xFF9E9E9E)
                                        )
                                    },
                                    isError = formErrors.courseName != null,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                formErrors.courseName?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Exam Type Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Exam Type",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                ExposedDropdownMenuBox(
                                    expanded = isDropdownExpanded,
                                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedExamType,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = {
                                            Text(
                                                text = "Select exam type",
                                                color = Color(0xFF9E9E9E)
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.Gray
                                            )
                                        },
                                        isError = formErrors.examType != null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false }
                                    ) {
                                        examTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type) },
                                                onClick = {
                                                    selectedExamType = type
                                                    isDropdownExpanded = false
                                                    if (formErrors.examType != null) {
                                                        viewModel.clearFormErrors()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                formErrors.examType?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Number of Questions & Duration Row Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Number of Questions
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Questions",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    OutlinedTextField(
                                        value = numberOfQuestions,
                                        onValueChange = {
                                            if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                                numberOfQuestions = it
                                                if (formErrors.questionCount != null) {
                                                    viewModel.clearFormErrors()
                                                }
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                text = "No. of questions",
                                                color = Color(0xFF9E9E9E)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = formErrors.questionCount != null,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    formErrors.questionCount?.let {
                                        Text(
                                            text = it,
                                            color = Color.Red,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                // Duration
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Duration (mins)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    OutlinedTextField(
                                        value = duration,
                                        onValueChange = {
                                            if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                                duration = it
                                                if (formErrors.duration != null) {
                                                    viewModel.clearFormErrors()
                                                }
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                text = "Duration",
                                                color = Color(0xFF9E9E9E)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = formErrors.duration != null,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    formErrors.duration?.let {
                                        Text(
                                            text = it,
                                            color = Color.Red,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Course Code Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Course Code",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = courseCode,
                                    onValueChange = {
                                        courseCode = it.uppercase()
                                        if (formErrors.courseCode != null) {
                                            viewModel.clearFormErrors()
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            text = "Enter course code",
                                            color = Color(0xFF9E9E9E)
                                        )
                                    },
                                    isError = formErrors.courseCode != null,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                formErrors.courseCode?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // File Upload Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Eligible Students (CSV)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Button(
                                    onClick = {  },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF5F5F5),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedFile?.name ?: "Choose CSV file",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                formErrors.tutorialListFile?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Create Button - matching DashboardScreen button style
                        Button(
                            onClick = {
                                viewModel.createExam(
                                    courseName = examName,
                                    courseCode = courseCode,
                                    duration = duration,
                                    questionCount = numberOfQuestions,
                                    examType = selectedExamType,
                                    tutorialListFile = selectedFile
                                )
                            },
                            enabled = examCreationState !is CreateExamViewModel.ExamCreationState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryBlue,
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (examCreationState is CreateExamViewModel.ExamCreationState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (examCreationState is CreateExamViewModel.ExamCreationState.Loading)
                                    "Creating..." else "Create Exam",
                                color = Color.White,
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