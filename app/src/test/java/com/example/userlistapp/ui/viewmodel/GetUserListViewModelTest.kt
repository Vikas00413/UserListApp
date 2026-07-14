package com.example.userlistapp.ui.viewmodel

import com.example.userlistapp.TestData
import com.example.userlistapp.domain.usecase.UserListUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetUserListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val useCase: UserListUseCase = mock()
    private lateinit var viewModel: GetUserListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GetUserListViewModel(userListUseCase = useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUser updates state with users on success`() = runTest(testDispatcher) {
        val users = listOf(TestData.user(id = 1), TestData.user(id = 2, name = "John"))
        whenever(useCase.getUserList(currentPage = eq(1), limit = eq(20))).thenReturn(
            TestData.userListResponse(total = 100, users = users),
        )

        viewModel.handleIntent(UerListIntent.LoadUser)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(users, state.users)
        assertTrue(viewModel.hasMore)
        assertEquals(2, viewModel.currentPage)
    }

    @Test
    fun `loadUser emits error effect on failure`() = runTest(testDispatcher) {
        whenever(useCase.getUserList(any(), any())).thenThrow(RuntimeException("network error"))

        val effects = mutableListOf<UserListEvent>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.handleIntent(UerListIntent.LoadUser)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.users.isEmpty())
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, effects.size)
        assertEquals(UserListEvent.ShowError("network error"), effects.first())

        collectJob.cancel()
    }

    @Test
    fun `loadNextPage appends users when more data is available`() = runTest(testDispatcher) {
        val firstPage = listOf(TestData.user(id = 1))
        val secondPage = listOf(TestData.user(id = 2, name = "John"))
        whenever(useCase.getUserList(currentPage = eq(1), limit = eq(20))).thenReturn(
            TestData.userListResponse(total = 100, users = firstPage),
        )
        whenever(useCase.getUserList(currentPage = eq(2), limit = eq(20))).thenReturn(
            TestData.userListResponse(total = 100, users = secondPage),
        )

        viewModel.handleIntent(UerListIntent.LoadUser)
        advanceUntilIdle()

        viewModel.handleIntent(UerListIntent.LoadNext)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoadingMore)
        assertEquals(listOf(secondPage.first(), firstPage.first()), state.users)
        assertTrue(state.canLoadMore)
        assertEquals(3, viewModel.currentPage)
    }

    @Test
    fun `loadNextPage skips duplicate requests while loading`() = runTest(testDispatcher) {
        whenever(useCase.getUserList(currentPage = eq(1), limit = eq(20))).thenReturn(
            TestData.userListResponse(),
        )

        val secondPageGate = CompletableDeferred<Unit>()
        doAnswer {
            runBlocking { secondPageGate.await() }
            TestData.userListResponse(users = listOf(TestData.user(id = 2, name = "John")))
        }.whenever(useCase).getUserList(currentPage = eq(2), limit = eq(20))

        viewModel.handleIntent(UerListIntent.LoadUser)
        advanceUntilIdle()

        viewModel.handleIntent(UerListIntent.LoadNext)
        viewModel.handleIntent(UerListIntent.LoadNext)

        secondPageGate.complete(Unit)
        advanceUntilIdle()

        verify(useCase, times(1)).getUserList(currentPage = eq(2), limit = eq(20))
    }

    @Test
    fun `loadNextPage does nothing when no more pages`() = runTest(testDispatcher) {
        val users = listOf(TestData.user())
        whenever(useCase.getUserList(currentPage = eq(1), limit = eq(20))).thenReturn(
            TestData.userListResponse(total = 1, users = users),
        )

        viewModel.handleIntent(UerListIntent.LoadUser)
        advanceUntilIdle()

        assertFalse(viewModel.hasMore)

        viewModel.handleIntent(UerListIntent.LoadNext)
        advanceUntilIdle()

        assertEquals(users, viewModel.state.value.users)
        assertEquals(2, viewModel.currentPage)
    }
}
