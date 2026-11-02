package com.example.secretsanta.domain.model

/**
 * The result of the draw for one participant: [giver] buys a present for [receiver].
 *
 * @property revealed whether the giver already saw the result (local mode) or it was sent (online mode).
 */
data class Assignment(
    val giver: Participant,
    val receiver: Participant,
    val revealed: Boolean = false,
)
