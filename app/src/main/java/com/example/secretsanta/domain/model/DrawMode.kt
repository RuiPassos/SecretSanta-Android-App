package com.example.secretsanta.domain.model

/**
 * How the results of a draw are delivered to the participants.
 */
enum class DrawMode {
    /** Everyone is in the same room and the phone is passed around to reveal each result. */
    LOCAL,

    /** Every participant receives the result remotely (email, SMS or a messenger). */
    ONLINE,
}
