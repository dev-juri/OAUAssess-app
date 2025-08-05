package com.oau.assess.screens.admin.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    onCreateExam: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val primaryBlue = Color(0xFF2196F3)

    var examName by remember { mutableStateOf("") }
    var selectedExamType by remember { mutableStateOf("") }
    var numberOfQuestions by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Exams") }

    val examTypes = listOf("Multiple Choice", "Open-ended", "Mixed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
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
            listOf("Dashboard", "Exams", "Results", "Settings").forEach { tab ->
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
                .padding(24.dp)
        ) {
            // Title
            Text(
                text = "Create New Exam",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Form Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Exam Name
                Column {
                    Text(
                        text = "Exam Name",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        placeholder = {
                            Text(
                                text = "Enter exam name",
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = primaryBlue,
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }

                // Exam Type
                Column {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedBorderColor = primaryBlue,
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            examTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedExamType = type
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Number of Questions
                Column {
                    Text(
                        text = "Number of Questions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = numberOfQuestions,
                        onValueChange = { numberOfQuestions = it },
                        placeholder = {
                            Text(
                                text = "Enter number of questions",
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = primaryBlue,
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }

                // Duration
                Column {
                    Text(
                        text = "Duration (minutes)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        placeholder = {
                            Text(
                                text = "Enter exam duration",
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = primaryBlue,
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }

                // Course Code
                Column {
                    Text(
                        text = "Course Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        placeholder = {
                            Text(
                                text = "Enter course code",
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = primaryBlue,
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }

                // Upload Eligible Students
                Column {
                    Text(
                        text = "Upload Eligible Students (CSV)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                1.dp,
                                Color(0xFFE0E0E0),
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                Color(0xFFFAFAFA),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { /* Handle file selection */ }
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (selectedFile.isEmpty()) "Choose file" else selectedFile,
                            color = if (selectedFile.isEmpty()) Color(0xFF9E9E9E) else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create Exam Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onCreateExam,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Create Exam",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}