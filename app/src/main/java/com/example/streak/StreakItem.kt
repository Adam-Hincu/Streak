package com.example.streak

import org.json.JSONObject
import java.util.UUID

/**
 * StreakItem now stores only the start time, paused state, and total paused duration.
 * The timer is always calculated from these fields and the current system time.
 */
data class StreakItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    var isActive: Boolean = true,
    var startTimeMillis: Long = System.currentTimeMillis(),
    var pausedDurationMillis: Long = 0L,
    var lastPausedAt: Long? = null,
    var type: String = "timer", // "timer" or "counter"
    var counterValue: Int = 0 // Only used for counter streaks
) {
    fun getElapsedMillis(now: Long = System.currentTimeMillis()): Long {
        return if (isActive) {
            (now - startTimeMillis - pausedDurationMillis)
        } else {
            (lastPausedAt ?: now) - startTimeMillis - pausedDurationMillis
        }
    }

    fun getTimeParts(now: Long = System.currentTimeMillis()): TimeParts {
        val totalSeconds = getElapsedMillis(now) / 1000
        val days = (totalSeconds / 86400).toInt()
        val hours = ((totalSeconds % 86400) / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        return TimeParts(days, hours, minutes, seconds)
    }

    fun pause(now: Long = System.currentTimeMillis()) {
        if (isActive) {
            isActive = false
            lastPausedAt = now
        }
    }

    fun resume(now: Long = System.currentTimeMillis()) {
        if (!isActive) {
            isActive = true
            pausedDurationMillis += (now - (lastPausedAt ?: now))
            lastPausedAt = null
        }
    }

    fun reset(now: Long = System.currentTimeMillis()) {
        startTimeMillis = now
        pausedDurationMillis = 0L
        lastPausedAt = if (isActive) null else now
    }

    fun incrementCounter(amount: Int = 1) {
        if (type == "counter") counterValue += amount
    }

    fun decrementCounter(amount: Int = 1) {
        if (type == "counter") counterValue -= amount
        if (counterValue < 0) counterValue = 0
    }

    fun resetCounter() {
        if (type == "counter") counterValue = 0
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("isActive", isActive)
            put("startTimeMillis", startTimeMillis)
            put("pausedDurationMillis", pausedDurationMillis)
            put("lastPausedAt", lastPausedAt)
            put("type", type)
            put("counterValue", counterValue)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): StreakItem {
            return StreakItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.getString("title"),
                isActive = obj.optBoolean("isActive", true),
                startTimeMillis = obj.optLong("startTimeMillis", System.currentTimeMillis()),
                pausedDurationMillis = obj.optLong("pausedDurationMillis", 0L),
                lastPausedAt = if (obj.has("lastPausedAt") && !obj.isNull("lastPausedAt")) obj.getLong("lastPausedAt") else null,
                type = obj.optString("type", "timer"),
                counterValue = obj.optInt("counterValue", 0)
            )
        }
    }
}

data class TimeParts(val days: Int, val hours: Int, val minutes: Int, val seconds: Int) 