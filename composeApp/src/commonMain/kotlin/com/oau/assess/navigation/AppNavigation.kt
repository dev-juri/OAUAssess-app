package com.oau.assess.navigation


import DashboardScreen
import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.oau.assess.models.Exam
import com.oau.assess.screens.admin.dashboard.AdminDashboardScreen
import com.oau.assess.screens.admin.exam.CreateExamScreen
import com.oau.assess.screens.admin.exam.UpdateMcqExamScreen
import com.oau.assess.screens.admin.exam.UpdateOeExamScreen
import com.oau.assess.screens.admin.login.AdminLoginScreen
import com.oau.assess.screens.student.test.McqExamScreen
import com.oau.assess.screens.student.test.OpenEndedExamScreen
import com.oau.assess.utils.ScreenExamType
import org.koin.compose.KoinContext

@Composable
fun AppNavigation(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    KoinContext {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.Login
        ) {
            composable<Screen.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Login) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Dashboard> {
                DashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true } // Clear entire navigation stack
                        }
                    },
                    onExamClick = { studentId, examId, examTitle, examType, duration ->
                        when (examType) {
                            "OeQuestion" -> {
                                navController.navigate(
                                    Screen.OpenEndedExam(
                                        studentId = studentId,
                                        examId = examId,
                                        examTitle = examTitle,
                                        duration = duration
                                    )
                                )
                            }

                            else -> {
                                navController.navigate(
                                    Screen.McqExam(
                                        studentId = studentId,
                                        examId = examId,
                                        examTitle = examTitle,
                                        duration = duration
                                    )
                                )
                            }
                        }
                    }
                )
            }

            composable<Screen.McqExam> { backStackEntry ->
                val mcqExam = backStackEntry.toRoute<Screen.McqExam>()

                McqExamScreen(
                    onLogout = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true } // Clear entire navigation stack
                        }
                    },
                    examId = mcqExam.examId,
                    examTitle = mcqExam.examTitle,
                    totalDuration = mcqExam.duration,
                    onExamComplete = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Dashboard) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.OpenEndedExam> { backStackEntry ->
                val openEndedExam = backStackEntry.toRoute<Screen.OpenEndedExam>()
                OpenEndedExamScreen(
                    onLogout = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true } // Clear entire navigation stack
                        }
                    },
                    examId = openEndedExam.examId,
                    examTitle = openEndedExam.examTitle,
                    totalDuration = openEndedExam.duration,
                    onExamComplete = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Dashboard) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Admin flow
            composable<Screen.AdminLogin> {
                AdminLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.AdminLogin) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.AdminDashboard> {
                AdminDashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.AdminLogin) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onCreateExam = {
                        navController.navigate(Screen.CreateExam)
                    },
                    onUpdateExam = { exam ->
                        when (exam.examType.lowercase()) {
                            ScreenExamType.McqQuestion.name -> {
                                navController.navigate(
                                    Screen.UpdateMCQExam(
                                        examId = exam.id,
                                        courseName = exam.courseName,
                                        courseCode = exam.courseCode,
                                        duration = exam.duration,
                                        questionCount = exam.questionCount,
                                        examType = exam.examType
                                    )
                                )
                            }

                            ScreenExamType.OeQuestion.name -> {
                                navController.navigate(
                                    Screen.UpdateOpenEndedExam(
                                        examId = exam.id,
                                        courseName = exam.courseName,
                                        courseCode = exam.courseCode,
                                        duration = exam.duration,
                                        examType = exam.examType
                                    )
                                )
                            }

                            else -> {
                                // Default to MCQ if exam type is not recognized
                                navController.navigate(
                                    Screen.UpdateMCQExam(
                                        examId = exam.id,
                                        courseName = exam.courseName,
                                        courseCode = exam.courseCode,
                                        duration = exam.duration,
                                        questionCount = exam.questionCount,
                                        examType = exam.examType
                                    )
                                )
                            }
                        }
                    }
                )
            }

            composable<Screen.CreateExam> {
                CreateExamScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.AdminLogin) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onExamCreated = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // Exam Update Screens
            composable<Screen.UpdateMCQExam> { backStackEntry ->
                val updateMCQExam = backStackEntry.toRoute<Screen.UpdateMCQExam>()

                // Create exam object from navigation parameters
                val exam = Exam(
                    id = updateMCQExam.examId,
                    courseName = updateMCQExam.courseName,
                    courseCode = updateMCQExam.courseCode,
                    duration = updateMCQExam.duration,
                    questionCount = updateMCQExam.questionCount,
                    examType = updateMCQExam.examType,
                    questions = emptyList(),
                    createdAt = "",
                    updatedAt = "",
                    version = 0
                )

                UpdateMcqExamScreen(
                    exam = exam,
                    onBackPressed = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.AdminDashboard) { inclusive = true }
                        }
                    },
                    onFileSelected = { fileData, fileName ->
                        // Handle file selection if needed for preview or validation
                    },
                    onUpdateExam = { fileData, fileName ->
                        // Handle exam update logic here
                        // Call your repository/API to update the exam with the file
                        // After successful update, navigate back to dashboard
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.AdminDashboard) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.UpdateOpenEndedExam> { backStackEntry ->
                val updateOpenEndedExam = backStackEntry.toRoute<Screen.UpdateOpenEndedExam>()

                // Create exam object from navigation parameters
                val exam = Exam(
                    id = updateOpenEndedExam.examId,
                    courseName = updateOpenEndedExam.courseName,
                    courseCode = updateOpenEndedExam.courseCode,
                    duration = updateOpenEndedExam.duration,
                    questionCount = 0, // Not relevant for open-ended exams
                    examType = updateOpenEndedExam.examType,
                    questions = emptyList(),
                    createdAt = "",
                    updatedAt = "",
                    version = 0
                )

                UpdateOeExamScreen(
                    exam = exam,
                    onBackPressed = {
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.AdminDashboard) { inclusive = true }
                        }
                    },
                    onQuestionsFileSelected = { fileData, fileName ->
                        // Handle questions file selection if needed
                    },
                    onAnswerKeyFileSelected = { fileData, fileName ->
                        // Handle answer key file selection if needed
                    },
                    onUpdateExam = { questionsFileData, questionsFileName, answerKeyFileData, answerKeyFileName ->
                        // Handle exam update logic here
                        // Call your repository/API to update the exam with the files
                        // After successful update, navigate back to dashboard
                        navController.navigate(Screen.AdminDashboard) {
                            popUpTo(Screen.AdminDashboard) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Let external targets bind navigation if needed (e.g. for web back/forward support)
        LaunchedEffect(navController) {
            onNavHostReady(navController)
        }
    }
}