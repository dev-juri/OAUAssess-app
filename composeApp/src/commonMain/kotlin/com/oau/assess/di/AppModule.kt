package com.oau.assess.di

import LoginViewModel
import com.oau.assess.repositories.StudentRepository
import com.oau.assess.repositories.StudentRepositoryImpl
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
    single<LoginViewModel> { LoginViewModel(get<StudentRepository>()) }
    single<DashboardViewModel> { DashboardViewModel(get()) }
    single<ExamViewModel> { ExamViewModel(get()) }
}

val appModule = listOf(
    networkModule,
    repositoryModule
)