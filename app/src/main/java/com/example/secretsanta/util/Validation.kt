package com.example.secretsanta.util

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val PHONE_REGEX = Regex("^\\+?[0-9 ()-]{6,20}$")

fun String.isValidEmail(): Boolean = EMAIL_REGEX.matches(trim())

fun String.isValidPhone(): Boolean = PHONE_REGEX.matches(trim())
