package com.example.secretsanta.domain.draw

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SecretSantaDrawerTest {

    private val drawer = SecretSantaDrawer(Random(42))

    @Test
    fun `fewer than three participants is not enough`() {
        assertEquals(DrawResult.NotEnoughParticipants, drawer.draw(emptyList()))
        assertEquals(DrawResult.NotEnoughParticipants, drawer.draw(listOf(1L, 2L)))
    }

    @Test
    fun `duplicated ids are counted once`() {
        assertEquals(DrawResult.NotEnoughParticipants, drawer.draw(listOf(1L, 1L, 2L)))
    }

    @Test
    fun `every participant gives and receives exactly one present and never to themselves`() {
        val ids = (1L..20L).toList()
        repeat(200) {
            val pairs = drawer.draw(ids).successPairs()
            assertEquals(ids.toSet(), pairs.keys)
            assertEquals(ids.toSet(), pairs.values.toSet())
            pairs.forEach { (giver, receiver) -> assertNotEquals(giver, receiver) }
        }
    }

    @Test
    fun `exclusions are always respected`() {
        val ids = listOf(1L, 2L, 3L, 4L, 5L, 6L)
        // Two couples that must not draw each other, plus last year's pair 5 -> 6.
        val exclusions = mapOf(
            1L to setOf(2L),
            2L to setOf(1L),
            3L to setOf(4L),
            4L to setOf(3L),
            5L to setOf(6L),
        )
        repeat(500) {
            val pairs = drawer.draw(ids, exclusions).successPairs()
            pairs.forEach { (giver, receiver) ->
                assertTrue("$giver must not draw $receiver", receiver !in exclusions[giver].orEmpty())
            }
        }
    }

    @Test
    fun `participant excluding everybody makes the draw impossible`() {
        val result = drawer.draw(listOf(1L, 2L, 3L), mapOf(1L to setOf(2L, 3L)))
        assertEquals(DrawResult.Impossible(setOf(1L)), result)
    }

    @Test
    fun `impossible combination without an obviously blocked person is detected`() {
        // 1 and 2 can only draw 3, so one of them is always left without a giftee.
        val result = drawer.draw(
            participantIds = listOf(1L, 2L, 3L, 4L),
            exclusions = mapOf(1L to setOf(2L, 4L), 2L to setOf(1L, 4L)),
        )
        assertTrue(result is DrawResult.Impossible)
    }

    @Test
    fun `matching fallback finds the only valid draw`() {
        val strictDrawer = SecretSantaDrawer(Random(7), maxRandomAttempts = 0)
        // The only valid solution is the cycle 1 -> 2 -> 3 -> 4 -> 1.
        val exclusions = mapOf(
            1L to setOf(3L, 4L),
            2L to setOf(1L, 4L),
            3L to setOf(1L, 2L),
            4L to setOf(2L, 3L),
        )
        val pairs = strictDrawer.draw(listOf(1L, 2L, 3L, 4L), exclusions).successPairs()
        assertEquals(mapOf(1L to 2L, 2L to 3L, 3L to 4L, 4L to 1L), pairs)
    }

    @Test
    fun `matching fallback respects the rules for large groups`() {
        val strictDrawer = SecretSantaDrawer(Random(1), maxRandomAttempts = 0)
        val ids = (1L..50L).toList()
        val exclusions = ids.associateWith { id -> setOf(id % 50 + 1) }
        val pairs = strictDrawer.draw(ids, exclusions).successPairs()
        assertEquals(ids.toSet(), pairs.values.toSet())
        pairs.forEach { (giver, receiver) ->
            assertNotEquals(giver, receiver)
            assertTrue(receiver !in exclusions.getValue(giver))
        }
    }

    @Test
    fun `results are random`() {
        val ids = (1L..6L).toList()
        val distinctResults = (1..50).map { drawer.draw(ids).successPairs() }.toSet()
        assertTrue("Expected different draws, got ${distinctResults.size}", distinctResults.size > 10)
    }

    @Test
    fun `same seed gives the same draw`() {
        val ids = (1L..10L).toList()
        assertEquals(
            SecretSantaDrawer(Random(99)).draw(ids),
            SecretSantaDrawer(Random(99)).draw(ids),
        )
    }

    private fun DrawResult.successPairs(): Map<Long, Long> {
        assertTrue("Expected success but was $this", this is DrawResult.Success)
        return (this as DrawResult.Success).pairs
    }
}
