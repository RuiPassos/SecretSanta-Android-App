package com.example.secretsanta.domain.draw

sealed interface DrawResult {
    /** Maps every giver id to the receiver id. */
    data class Success(val pairs: Map<Long, Long>) : DrawResult

    data object NotEnoughParticipants : DrawResult

    /** The exclusions make it impossible for everybody to get a valid match. */
    data class Impossible(val blockedParticipantIds: Set<Long>) : DrawResult
}
