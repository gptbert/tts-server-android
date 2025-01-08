package com.github.jing332.tts_server_android.model.speech.tts

import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.roundToLong

object EdgeTtsDrm {
    private const val WIN_EPOCH = 11644473600L
    private const val S_TO_NS = 1_000_000_000.0
    private const val TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"

    fun getTrustedClientToken(): String = TRUSTED_CLIENT_TOKEN

    private var clockSkewSeconds: Double = 0.0

    fun adjustClockSkewSeconds(skewSeconds: Double) {
        clockSkewSeconds += skewSeconds
    }

    private fun getUnixTimestamp(): Double {
        return Instant.now().epochSecond.toDouble() + clockSkewSeconds
    }

    fun generateSecMsGec(): String {
        // Get the current timestamp in Unix format with clock skew correction
        var ticks = getUnixTimestamp()

        // Switch to Windows file time epoch (1601-01-01 00:00:00 UTC)
        ticks += WIN_EPOCH

        // Round down to the nearest 5 minutes (300 seconds)
        ticks -= ticks % 300

        // Convert the ticks to 100-nanosecond intervals (Windows file time format)
        ticks *= S_TO_NS / 100

        // Create the string to hash by concatenating the ticks and the trusted client token
        val strToHash = "${ticks.roundToLong()}$TRUSTED_CLIENT_TOKEN"

        // Compute the SHA256 hash and return the uppercased hex digest
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(strToHash.toByteArray(Charsets.US_ASCII))
        return hashBytes.joinToString("") { "%02X".format(it) }
    }

    fun generateSecMsGecVersion(): String = "1.1.1"
}
