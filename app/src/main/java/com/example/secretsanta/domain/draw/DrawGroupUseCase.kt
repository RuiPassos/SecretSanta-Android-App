package com.example.secretsanta.domain.draw

import com.example.secretsanta.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Runs the draw for a group with its current participants and exclusions and stores the result.
 */
class DrawGroupUseCase @Inject constructor(
    private val repository: GroupRepository,
    private val drawer: SecretSantaDrawer,
) {
    suspend operator fun invoke(groupId: Long): DrawResult {
        val details = repository.observeGroupDetails(groupId).first()
            ?: return DrawResult.NotEnoughParticipants
        val participants = details.participants
        val result = drawer.draw(
            participantIds = participants.map { it.id },
            exclusions = participants.associate { it.id to it.excludedIds },
        )
        if (result is DrawResult.Success) {
            repository.saveDraw(groupId, result.pairs)
        }
        return result
    }
}
