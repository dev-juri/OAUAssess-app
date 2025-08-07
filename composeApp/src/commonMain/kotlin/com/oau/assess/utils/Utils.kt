package com.oau.assess.utils

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes : $remainingSeconds"
}

enum class ExamType {
    MCQ,
    OE
}

enum class ScreenExamType(val examType : String) {
    McqQuestion("Multiple Choice"),
    OeQuestion("Open-Ended")
}

@OptIn(ExperimentalWasmJsInterop::class)
suspend fun readFileAsByteArray(file: File): ByteArray {
    return suspendCoroutine { continuation ->
        val reader = FileReader()

        reader.onload = {
            try {
                val arrayBuffer = reader.result as ArrayBuffer
                val int8Array = Int8Array(arrayBuffer)
                val byteArray = ByteArray(int8Array.length) { i -> return@ByteArray int8Array[i] }
                continuation.resume(byteArray)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        reader.onerror = {
            continuation.resumeWithException(Exception("Failed to read file"))
        }

        reader.readAsArrayBuffer(file)
    }
}