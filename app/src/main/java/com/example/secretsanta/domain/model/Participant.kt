package com.example.secretsanta.domain.model

/**
 * A person taking part in a [Group].
 *
 * @property excludedIds ids of the participants this person must never draw ("Anti-Secret-Santas").
 */
data class Participant(
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val wishes: String = "",
    val excludedIds: Set<Long> = emptySet(),
)
