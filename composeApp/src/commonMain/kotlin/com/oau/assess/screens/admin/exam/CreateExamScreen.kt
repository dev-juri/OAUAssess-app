package com.oau.assess.screens.admin.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.oau.assess.utils.ScreenExamType
import org.w3c.files.File
import org.w3c.dom.HTMLInputElement
import kotlinx.browser.document
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalWasmJsInterop::class)
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
    var selectedExamType by remember { mutableStateOf<ScreenExamType?>(null) }
    var numberOfQuestions by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Define colors matching DashboardScreen
    val primaryBlue = Color(0xFF2196F3)
    val lightGray = Color(0xFFF5F5F5)

    val examTypes = listOf<ScreenExamType>(ScreenExamType.McqQuestion, ScreenExamType.OeQuestion)

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
                input.accept = ".xlsx,.xls"

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
                // Handle error silently
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
                // Top Navigation Bar with Back Button
                TopAppBar(
                    title = {
                        Text(
                            text = "Create New Exam",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.logoutAdmin()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Logout",
                                tint = Color.Red
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )

                // Scrollable Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Subtitle
                    Text(
                        text = "Fill in the details below to create a new exam for students.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Form Fields in Cards
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
                                modifier = Modifier.padding(20.dp)
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
                                modifier = Modifier.padding(20.dp)
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
                                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedExamType?.examType ?: "",
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
                                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                            .clickable { isDropdownExpanded = true },
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false }
                                    ) {
                                        examTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type.examType) },
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
                                    .padding(20.dp),
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
                                modifier = Modifier.padding(20.dp)
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
                                            text = "Enter course code (e.g., CSC301)",
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
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Eligible Students List",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Upload a CSV file containing student information",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Button(
                                    onClick = handleFileSelection,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedFile != null) Color(0xFFE8F5E8) else Color(0xFFF5F5F5),
                                        contentColor = if (selectedFile != null) Color(0xFF2E7D32) else Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedFile?.name ?: "Choose CSV/Excel file",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 4.dp)
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // Create Button
                        Button(
                            onClick = {
                                viewModel.createExam(
                                    courseName = examName,
                                    courseCode = courseCode,
                                    duration = duration,
                                    questionCount = numberOfQuestions,
                                    examType = selectedExamType!!.name,
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
                                .height(52.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
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
                                        "Creating Exam..." else "Create Exam",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Bottom spacing for better scrolling experience
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}