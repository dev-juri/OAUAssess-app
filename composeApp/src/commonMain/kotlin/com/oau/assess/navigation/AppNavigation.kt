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
                    onLoginSuccess = { student ->
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Login) { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Dashboard> { backStackEntry ->
                //val student = backStackEntry.toRoute<Screen.Dashboard>()

                DashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true } // Clear entire navigation stack
                        }
                    },
                    onExamClick = { examId ->
                        //navController.navigate(Screen.ExamDetail(examId, student))
                    }
                )
            }

            /*        composable<Screen.ExamDetail> { backStackEntry ->
                        val route = backStackEntry.toRoute<Screen.ExamDetail>()
                        val examId = route.examId
                        val student = route.student

                        ExamDetailScreen(
                            examId = examId,
                            student = student,
                            onBack = {
                                navController.popBackStack()
                            },
                            onLogout = {
                                navController.navigate(Screen.Login) {
                                    popUpTo(0) { inclusive = true } // Clear entire navigation stack
                                }
                            }
                        )
                    }*/
        }

        // Let external targets bind navigation if needed (e.g. for web back/forward support)
        LaunchedEffect(navController) {
            onNavHostReady(navController)
        }
    }
}
