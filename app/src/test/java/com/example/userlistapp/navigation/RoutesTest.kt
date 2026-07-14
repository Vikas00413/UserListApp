package com.example.userlistapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {

    @Test
    fun `userDetail builds route with user id`() {
        assertEquals("user_detail/42", Routes.userDetail(userId = 42))
    }
}
