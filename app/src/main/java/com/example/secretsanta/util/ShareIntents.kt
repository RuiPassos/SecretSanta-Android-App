package com.example.secretsanta.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Hands the result over to an email app, the SMS app or any messenger. The app itself never
 * sends anything, so no account, server or extra permission is needed.
 */
object ShareIntents {

    fun email(to: String, subject: String, body: String) = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    fun sms(phone: String, body: String) = Intent(Intent.ACTION_SENDTO).apply {
        data = "smsto:${phone.filterNot(Char::isWhitespace)}".toUri()
        putExtra("sms_body", body)
    }

    fun share(subject: String, body: String, chooserTitle: String): Intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        },
        chooserTitle,
    )
}

/** @return `false` when no installed app can handle the intent. */
fun Context.tryStartActivity(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}
