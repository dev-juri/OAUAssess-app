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

            composable<Screen.Dashboard> { backStackEntry ->
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
                            else -> { // McqQuestion or any other type defaults to MCQ
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
                    examTitle = mcqExam.examTitle,
                    totalDuration = mcqExam.duration,
                    onExamComplete = { answers ->
                        // Handle exam completion
                        // You might want to navigate to a results screen or back to dashboard
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Dashboard) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.OpenEndedExam> { backStackEntry ->
                val openEndedExam = backStackEntry.toRoute<Screen.OpenEndedExam>()
                OpenEndedExamScreen(
                    examTitle = openEndedExam.examTitle,
                    totalDuration = openEndedExam.duration,
                    onSubmit = { answers ->
                        // Handle exam submission
                        // You might want to navigate to a results screen or back to dashboard
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Dashboard) { inclusive = true }
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