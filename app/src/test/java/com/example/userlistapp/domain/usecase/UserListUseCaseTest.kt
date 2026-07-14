package com.example.userlistapp.domain.usecase

import com.example.userlistapp.TestData
import com.example.userlistapp.domain.repo.UserListRepo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserListUseCaseTest {

    private val repository: UserListRepo = mock()
    private val useCase = UserListUseCase(repository)

    @Test
    fun `getUserList delegates to repository`() = runTest {
        val expected = TestData.userListResponse(total = 30)
        whenever(repository.getUserList(currentPage = eq(1), limit = eq(20))).thenReturn(expected)

        val result = useCase.getUserList(currentPage = 1, limit = 20)

        verify(repository).getUserList(currentPage = eq(1), limit = eq(20))
        assertEquals(expected, result)
    }
}
