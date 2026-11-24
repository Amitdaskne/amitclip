package com.amit.amitclip.data

import android.util.Patterns
import kotlinx.coroutines.flow.Flow

class ClipRepository(private val dao: ClipDao) {

    fun allClips(): Flow<List<Clip>> = dao.getAllClips()

    fun searchClips(query: String): Flow<List<Clip>> =
        if (query.isBlank()) dao.getAllClips() else dao.searchClips(query)

    suspend fun addClip(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        dao.insert(Clip(text = trimmed, type = detectType(trimmed)))
        dao.pruneOldClips(keep = 200)
    }

    suspend fun delete(clip: Clip) = dao.delete(clip)

    suspend fun togglePin(clip: Clip) = dao.setPinned(clip.id, !clip.isPinned)

    private fun detectType(text: String): String = when {
        Patterns.WEB_URL.matcher(text).matches() -> "url"
        Patterns.EMAIL_ADDRESS.matcher(text).matches() -> "email"
        Patterns.PHONE.matcher(text).matches() -> "phone"
        else -> "text"
    }
}
