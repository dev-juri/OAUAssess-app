package com.oau.assess.di

import LoginViewModel
import com.oau.assess.repositories.admin.AdminRepository
import com.oau.assess.repositories.admin.AdminRepositoryImpl
import com.oau.assess.repositories.student.StudentRepository
import com.oau.assess.repositories.student.StudentRepositoryImpl
import com.oau.assess.screens.admin.dashboard.AdminDashboardViewModel
import com.oau.assess.screens.admin.exam.CreateExamViewModel
import com.oau.assess.screens.admin.exam.ExamUpdateViewModel
import com.oau.assess.screens.admin.login.AdminLoginViewModel
import com.oau.assess.screens.student.dashboard.DashboardViewModel
import com.oau.assess.screens.student.test.ExamViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
}

val repositoryModule = module {
    single<StudentRepository> { StudentRepositoryImpl(get<HttpClient>()) }
    single<AdminRepository> { AdminRepositoryImpl(get<HttpClient>()) }

    single<LoginViewModel> { LoginViewModel(get<StudentRepository>()) }
    single<DashboardViewModel> { DashboardViewModel(get<StudentRepository>()) }
    single<ExamViewModel> { ExamViewModel(get<StudentRepository>()) }

    single<AdminLoginViewModel> { AdminLoginViewModel(get<AdminRepository>()) }
    single<AdminDashboardViewModel> { AdminDashboardViewModel(get<AdminRepository>()) }
    single<CreateExamViewModel> { CreateExamViewModel(get<AdminRepository>()) }
    single<ExamUpdateViewModel> { ExamUpdateViewModel(get<AdminRepository>()) }
}

val appModule = listOf(
    networkModule,
    repositoryModule
)