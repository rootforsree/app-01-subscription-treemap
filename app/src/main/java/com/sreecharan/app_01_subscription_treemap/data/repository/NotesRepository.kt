package com.sreecharan.app_01_subscription_treemap.data.repository

import com.sreecharan.app_01_subscription_treemap.data.local.NoteDao
import com.sreecharan.app_01_subscription_treemap.data.local.NoteEntity
import com.sreecharan.app_01_subscription_treemap.data.model.Note
import com.sreecharan.app_01_subscription_treemap.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The only public entry into the notes data layer.
 * UI observes [observeNotes]; Room is the single source of truth.
 */
interface NotesRepository {

    fun observeNotes(): Flow<List<Note>>

    suspend fun addSampleNote()

    suspend fun clearNotes()
}

class NotesRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeNotes()
            .map { entities -> entities.map(NoteEntity::toDomain) }
            .flowOn(ioDispatcher)

    override suspend fun addSampleNote() {
        withContext(ioDispatcher) {
            noteDao.insert(
                NoteEntity(
                    title = "Sample note",
                    body = "This is a sample note. Delete me and build something great.",
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun clearNotes() {
        withContext(ioDispatcher) {
            noteDao.clearAll()
        }
    }

    private fun NoteEntity.toDomain(): Note = Note(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt,
    )
}
