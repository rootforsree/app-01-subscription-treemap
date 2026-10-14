package com.sreecharan.app_01_subscription_treemap

import app.cash.turbine.test
import com.sreecharan.app_01_subscription_treemap.data.model.Note
import com.sreecharan.app_01_subscription_treemap.data.repository.NotesRepository
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeUiState
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private lateinit var notesRepository: NotesRepository
    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        notesRepository = mockk()
        every { notesRepository.observeNotes() } returns notesFlow
        viewModel = HomeViewModel(notesRepository, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits loading then notes`() = runTest(testDispatcher) {
        val notes = listOf(Note(id = 1, title = "First", body = "Body", createdAt = 1_000))

        viewModel.uiState.test {
            assertEquals(HomeUiState(isLoading = true), awaitItem())

            notesFlow.value = notes

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(1, loaded.notes.size)
            assertEquals("First", loaded.notes.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh toggles isRefreshing true then false`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isRefreshing)

            viewModel.onRefresh()

            assertTrue(awaitItem().isRefreshing)
            assertFalse(awaitItem().isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry re-triggers load`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isRefreshing)

            viewModel.onRetry()

            assertTrue(awaitItem().isRefreshing)
            val done = awaitItem()
            assertFalse(done.isRefreshing)
            assertFalse(done.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
