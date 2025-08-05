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
import com.oau.assess.screens.admin.dashboard.AdminDashboardScreen
import com.oau.assess.screens.admin.exam.CreateExamScreen
import com.oau.assess.screens.admin.login.AdminLoginScreen
import com.oau.assess.screens.student.test.McqExamScreen
import com.oau.assess.screens.student.test.OpenEndedExamScreen
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
                    }
                )
            }

            composable<Screen.CreateExam> {
                CreateExamScreen(
                    onNavigateBack = {
                        navController.popBackStack()
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