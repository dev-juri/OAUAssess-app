package com.oau.assess.data

import androidx.compose.ui.graphics.Color

data class Exam(
    val id: String,
    val title: String,
    val date: String,
    val backgroundColor: Color,
    val isStarted: Boolean = false
)

