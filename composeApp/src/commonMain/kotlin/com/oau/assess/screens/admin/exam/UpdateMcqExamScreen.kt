package com.oau.assess.screens.admin.exam

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oau.assess.models.Exam
import com.oau.assess.screens.admin.exam.components.ExamFormFields
import com.oau.assess.utils.pickFile
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateMcqExamScreen(
    exam: Exam,
    onBackPressed: () -> Unit,
    onUpdateExam: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: ExamUpdateViewModel = koinInject<ExamUpdateViewModel>()
) {

    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is ExamUpdateUiState.Success -> {
                onUpdateExam()
            }

            else -> {}
        }
    }

    // Handle navigation to login if not authenticated
    LaunchedEffect(isAdminLoggedIn) {
        if (!isAdminLoggedIn) {
            onNavigateToLogin()
        }
    }

    // Show error dialog
    if (uiState is ExamUpdateUiState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text((uiState as ExamUpdateUiState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Reset state when screen is first opened
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OAU Assess", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                text = "Update MCQ Exam",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Disabled form fields
            ExamFormFields(exam = exam)

            // Upload Questions Section
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Upload Questions (Excel)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        pickFile(".xlsx,.xls") { file ->
                            viewModel.selectMcqFile(file)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF007BFF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Text(
                        text = selectedFiles.mcqFile?.name ?: "Choose Excel File",
                        fontSize = 16.sp
                    )
                }
            }

            // Update Button
            Button(
                onClick = {
                    viewModel.updateMcqExam(exam.id)
                },
                enabled = selectedFiles.mcqFile != null && !isLoading,
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