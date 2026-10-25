package com.riccardopinato.pocketguard.history

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.historyDataStore by preferencesDataStore(
    name = "pocket_guard_history"
)

data class GuardHistoryEvent(
    val timestamp: Long,
    val type: String,
    val detail: String
)

class HistoryRepository(
    private val context: Context
) {
    private val mutex = Mutex()
    private val key = stringPreferencesKey("events")

    val eventsFlow: Flow<List<GuardHistoryEvent>> =
        context.historyDataStore.data.map {
            decode(it[key].orEmpty())
        }

    suspend fun add(type: String, detail: String) {
        mutex.withLock {
            val current =
                decode(context.historyDataStore.data.first()[key].orEmpty())
                    .toMutableList()

            current.add(
                0,
                GuardHistoryEvent(
                    System.currentTimeMillis(),
                    sanitize(type),
                    sanitize(detail)
                )
            )

            context.historyDataStore.edit {
                it[key] = encode(current.take(MAX_EVENTS))
            }
        }
    }

    suspend fun clear() {
        context.historyDataStore.edit { it.remove(key) }
    }

    private fun encode(events: List<GuardHistoryEvent>): String =
        events.joinToString("\n") {
            "${it.timestamp}\t${sanitize(it.type)}\t${sanitize(it.detail)}"
        }

    private fun decode(raw: String): List<GuardHistoryEvent> {
        if (raw.isBlank()) return emptyList()

        return raw.lineSequence().mapNotNull { line ->
            val parts = line.split('\t', limit = 3)
            if (parts.size != 3) return@mapNotNull null
            val timestamp = parts[0].toLongOrNull() ?: return@mapNotNull null
            GuardHistoryEvent(timestamp, parts[1], parts[2])
        }.toList()
    }

    private fun sanitize(value: String): String =
        value.replace('\n', ' ').replace('\t', ' ')

    private companion object {
        const val MAX_EVENTS = 100
    }
}
