package com.example.secretsanta.domain.model

/**
 * Everything the UI needs to show a single group.
 */
data class GroupDetails(
    val group: Group,
    val participants: List<Participant>,
    val assignments: List<Assignment>,
) {
    fun assignmentFor(giverId: Long): Assignment? = assignments.firstOrNull { it.giver.id == giverId }
}
