package com.example.secretsanta.domain.draw

import javax.inject.Inject
import kotlin.random.Random

/**
 * Smart random generator for Secret Santa draws.
 *
 * Guarantees that:
 * - nobody draws themselves;
 * - nobody draws one of their excluded people ("Anti-Secret-Santas");
 * - every participant gives exactly one present and receives exactly one present.
 *
 * Strategy: first try plain random permutations (rejection sampling), which yields a uniformly
 * distributed result among all valid draws. When the constraints are very tight and random tries
 * keep failing, fall back to a randomised bipartite matching (Kuhn's algorithm), which always finds
 * a valid draw in polynomial time when one exists and proves impossibility otherwise.
 */
class SecretSantaDrawer(
    private val random: Random,
    private val maxRandomAttempts: Int = DEFAULT_RANDOM_ATTEMPTS,
) {
    @Inject
    constructor() : this(Random.Default)

    /**
     * @param participantIds ids of everybody in the group.
     * @param exclusions for each giver id, the receiver ids that are not allowed.
     */
    fun draw(
        participantIds: List<Long>,
        exclusions: Map<Long, Set<Long>> = emptyMap(),
    ): DrawResult {
        val ids = participantIds.distinct()
        if (ids.size < MIN_PARTICIPANTS) return DrawResult.NotEnoughParticipants

        val idSet = ids.toSet()
        val allowed: Map<Long, Set<Long>> = ids.associateWith { giver ->
            idSet - giver - exclusions[giver].orEmpty()
        }

        val blocked = allowed.filterValues { it.isEmpty() }.keys
        if (blocked.isNotEmpty()) return DrawResult.Impossible(blocked)

        repeat(maxRandomAttempts) {
            val receivers = ids.shuffled(random)
            if (ids.indices.all { receivers[it] in allowed.getValue(ids[it]) }) {
                return DrawResult.Success(ids.zip(receivers).toMap())
            }
        }

        return matchingDraw(ids, allowed)
    }

    private fun matchingDraw(ids: List<Long>, allowed: Map<Long, Set<Long>>): DrawResult {
        val candidates = allowed.mapValues { (_, receivers) -> receivers.shuffled(random) }
        val giverOf = HashMap<Long, Long>()

        fun tryAssign(giver: Long, visited: MutableSet<Long>): Boolean {
            for (receiver in candidates.getValue(giver)) {
                if (!visited.add(receiver)) continue
                val currentGiver = giverOf[receiver]
                if (currentGiver == null || tryAssign(currentGiver, visited)) {
                    giverOf[receiver] = giver
                    return true
                }
            }
            return false
        }

        val unmatched = ids.shuffled(random).filterNot { tryAssign(it, mutableSetOf()) }
        if (unmatched.isNotEmpty()) return DrawResult.Impossible(unmatched.toSet())

        return DrawResult.Success(giverOf.entries.associate { (receiver, giver) -> giver to receiver })
    }

    companion object {
        const val MIN_PARTICIPANTS = 3
        private const val DEFAULT_RANDOM_ATTEMPTS = 2_000
    }
}
