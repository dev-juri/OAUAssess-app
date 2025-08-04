package com.oau.assess.utils


fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "$minutes : $remainingSeconds"
}

enum class ExamType {
    MCQ,
    OE
}