package com.oau.assess.utils

import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
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

enum class ScreenExamType(val examType: String) {
    McqQuestion("Multiple Choice"),
    OeQuestion("Open-Ended")
}

@OptIn(ExperimentalWasmJsInterop::class)
fun pickFile(accept: String, onFileSelected: (File?) -> Unit) {
    try {
        val input = document.createElement("input")
            .unsafeCast<HTMLInputElement>()

        input.type = "file"
        input.accept = accept

        input.onchange = { event ->
            val target = event.target as HTMLInputElement
            val file = target.files?.item(0)
            onFileSelected(file)
        }

        input.onclick = {
            input.value = ""
        }

        input.click()
    } catch (_: Exception) {
        onFileSelected(null)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
suspend fun readFileAsByteArray(file: File): ByteArray {
    return suspendCoroutine { continuation ->
        val reader = FileReader()

        reader.onload = {
            try {
                val arrayBuffer = reader.result as ArrayBuffer
                val uint8Array = Uint8Array(arrayBuffer)
                val byteArray = ByteArray(uint8Array.length) { i ->
                    uint8Array[i]
                }
                continuation.resume(byteArray)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        reader.onerror = {
            continuation.resumeWithException(Exception("Failed to read file: ${reader.error}"))
        }

        reader.readAsArrayBuffer(file)
    }
}

fun extractFilenameFromHeader(contentDisposition: String?, isScoreSheet: Boolean): String =
    contentDisposition?.let {
        Regex("filename=\"([^\"]+)\"").find(it)?.groupValues?.get(1)
    } ?: if (isScoreSheet) "exam_score_sheet.xlsx" else "exam_scripts.zip"

@OptIn(ExperimentalWasmJsInterop::class)
fun saveFile(data: JsArray<JsAny?>, filename: String) {
    js(
        """
        const blob = new Blob([new Uint8Array(data)], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        """
    )
}

data class FileManager(
    val fileName: String,
    val mimeType: String,
    val fileContent: ByteArray
)
