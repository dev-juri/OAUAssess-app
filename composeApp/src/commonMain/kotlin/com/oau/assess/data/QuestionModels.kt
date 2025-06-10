package com.oau.assess.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Question {
    abstract val id: String
    abstract val question: String
}

@Serializable
data class McqQuestion(
    override val id: String,
    override val question: String,
    val options: List<String>
) : Question()

@Serializable
data class OeQuestion(
    override val id: String,
    override val question: String
) : Question()

// Helper data class for deserialization
@Serializable
data class QuestionDto(
    val id: String,
    val question: String,
    val options: List<String>? = null
) {
    fun toQuestion(): Question {
        return if (options != null) {
            McqQuestion(id, question, options)
        } else {
            OeQuestion(id, question)
        }
    }
}

@Serializable
data class AssignmentQuestionsResponse(
    val success: Boolean,
    val message: String,
    @SerialName("data")
    private val _data: List<QuestionDto>
) {
    val data: List<Question>
        get() = _data.map { it.toQuestion() }
}