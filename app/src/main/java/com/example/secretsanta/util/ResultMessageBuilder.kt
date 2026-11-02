package com.example.secretsanta.util

import android.content.Context
import com.example.secretsanta.R
import com.example.secretsanta.domain.model.Assignment
import com.example.secretsanta.domain.model.Group

/**
 * Builds the text that tells a participant who they have to buy a present for.
 */
class ResultMessageBuilder(private val context: Context) {

    fun subject(group: Group): String = context.getString(R.string.message_subject, group.name)

    fun body(group: Group, assignment: Assignment): String = buildString {
        appendLine(context.getString(R.string.message_greeting, assignment.giver.name))
        appendLine()
        appendLine(context.getString(R.string.message_result, group.name, assignment.receiver.name))

        val wishes = assignment.receiver.wishes
        if (wishes.isNotBlank()) {
            appendLine()
            appendLine(context.getString(R.string.message_wishes, assignment.receiver.name))
            appendLine(wishes)
        }

        val details = listOfNotNull(
            group.eventDate?.let { context.getString(R.string.message_date, it.formatMedium()) },
            group.budget.takeIf { it.isNotBlank() }?.let { context.getString(R.string.message_budget, it) },
            group.location.takeIf { it.isNotBlank() }?.let { context.getString(R.string.message_location, it) },
            group.notes.takeIf { it.isNotBlank() }?.let { context.getString(R.string.message_notes, it) },
        )
        if (details.isNotEmpty()) {
            appendLine()
            details.forEach(::appendLine)
        }

        appendLine()
        appendLine(context.getString(R.string.message_secret))
        append(context.getString(R.string.message_signature))
    }
}
