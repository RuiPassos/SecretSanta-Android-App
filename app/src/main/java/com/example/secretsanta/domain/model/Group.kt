package com.example.secretsanta.domain.model

import java.time.LocalDate

/**
 * A Secret Santa group (e.g. "Family Christmas 2026").
 *
 * @property drawnAt epoch millis of the last successful draw, or `null` if the draw hasn't happened yet.
 */
data class Group(
    val id: Long = 0,
    val name: String,
    val mode: DrawMode = DrawMode.LOCAL,
    val eventDate: LocalDate? = null,
    val budget: String = "",
    val location: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val drawnAt: Long? = null,
    val participantCount: Int = 0,
) {
    val isDrawn: Boolean get() = drawnAt != null
}
