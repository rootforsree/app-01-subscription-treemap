package com.sreecharan.app_01_subscription_treemap

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.sreecharan.app_01_subscription_treemap.data.local.AppDatabase
import com.sreecharan.app_01_subscription_treemap.data.repository.NotesRepository
import com.sreecharan.app_01_subscription_treemap.data.repository.NotesRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Room-backed repository test on Robolectric (JUnit4).
 * Written per the template contract; it has NOT been executed here
 * (no Gradle runs allowed in this environment). If CI shows flakiness,
 * delete this test per the zero-flaky rule.
 */
@RunWith(RobolectricTestRunner::class)
class NotesRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: NotesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NotesRepositoryImpl(database.noteDao(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert emits through observe`() = runTest {
        repository.observeNotes().test {
            assertTrue(awaitItem().isEmpty())

            repository.addSampleNote()

            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Sample note", notes.first().title)
            assertEquals(
                "This is a sample note. Delete me and build something great.",
                notes.first().body,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear removes all notes`() = runTest {
        repository.addSampleNote()
        repository.clearNotes()

        repository.observeNotes().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
