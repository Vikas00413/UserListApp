package com.example.userlistapp.data.repo

import com.example.userlistapp.TestData
import com.example.userlistapp.data.remote.ApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserListRepoImplTest {

    private val apiService: ApiService = mock()
    private val repository = UserListRepoImpl(apiService)

    @Test
    fun `getUserList fetches from api and maps to domain`() = runTest {
        val dto = TestData.userResponseDto(total = 50)
        whenever(apiService.getUsers(page = eq(2), limit = eq(10))).thenReturn(dto)

        val result = repository.getUserList(currentPage = 2, limit = 10)

        verify(apiService).getUsers(page = eq(2), limit = eq(10))
        assertEquals(50, result.total)
        assertEquals(1, result.users.size)
        assertEquals("Jane", result.users.first().name)
    }
}
