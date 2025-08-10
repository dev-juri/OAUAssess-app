import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.2.0-RC2"
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val baseUrl: String =
    (findProperty("BASE_URL") as String?)
        ?: System.getenv("BASE_URL")
        ?: localProperties.getProperty("BASE_URL")
        ?: "https://default-url.example.com/"

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin")
    val packageName = "com.oau.assess"

    inputs.property("BASE_URL", baseUrl)
    outputs.dir(outputDir)

    doLast {
        val file = outputDir.get().file("BuildConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package $packageName

            object BuildConfig {
                const val BASE_URL = "$baseUrl"
            }
            """.trimIndent()
        )
    }
}

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateBuildConfig.map { it.outputs.files.singleFile.parentFile })
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta04")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

                // Ktor Core
                implementation("io.ktor:ktor-client-core:3.1.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
                implementation("io.ktor:ktor-client-logging:3.1.3")

                implementation("io.ktor:ktor-client-cio:3.1.3")
                implementation("io.ktor:ktor-client-js:3.1.3")
                implementation("io.ktor:ktor-client-serialization:3.1.3")


                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

                // Koin DI
                implementation("io.insert-koin:koin-core:4.0.4")
                implementation("io.insert-koin:koin-compose:4.0.4")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.1.3")
                //implementation("org.jetbrains.kotlinx:kotlinx-browser:0.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.4")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-js:2025.8.6")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateBuildConfig")
}