package com.example.secretsanta.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun `valid emails are accepted`() {
        listOf("ana@mail.com", "first.last+tag@sub.domain.pt", " bruno@x.io ").forEach {
            assertTrue(it, it.isValidEmail())
        }
    }

    @Test
    fun `invalid emails are rejected`() {
        listOf("", "ana", "ana@", "@mail.com", "ana@mail", "ana mail@x.com").forEach {
            assertFalse(it, it.isValidEmail())
        }
    }

    @Test
    fun `phone numbers in common formats are accepted`() {
        listOf("912345678", "+351 912 345 678", "(21) 123-4567").forEach {
            assertTrue(it, it.isValidPhone())
        }
    }

    @Test
    fun `invalid phone numbers are rejected`() {
        listOf("", "123", "abc123456", "+351 912 345 678 999 999 999").forEach {
            assertFalse(it, it.isValidPhone())
        }
    }
}
