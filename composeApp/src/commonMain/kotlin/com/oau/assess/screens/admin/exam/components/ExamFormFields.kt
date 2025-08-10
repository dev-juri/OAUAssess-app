package com.oau.assess.screens.admin.exam.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oau.assess.models.Exam

@Composable
fun ExamFormFields(exam: Exam) {

    Column {
        // Exam Title
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Exam Title",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = exam.courseName,
                onValueChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledContainerColor = Color(0xFFF5F5F5) // Light grey background
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Course Code
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Course Code",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = exam.courseCode,
                onValueChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledContainerColor = Color(0xFFF5F5F5) // Light grey background
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Duration
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Duration (minutes)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = exam.duration.toString(),
                onValueChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledContainerColor = Color(0xFFF5F5F5) // Light grey background
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Exam Type
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Exam Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = exam.examType,
                onValueChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledContainerColor = Color(0xFFF5F5F5) // Light grey background
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

}