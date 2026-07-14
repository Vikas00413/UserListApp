package com.example.userlistapp.data

import com.example.userlistapp.TestData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DTOMapperTest {

    @Test
    fun `user dto maps to domain user`() {
        val dto = TestData.userDto(
            id = 7,
            firstName = "Alex",
            email = "alex@example.com",
            image = "https://example.com/alex.png",
        )

        val user = dto.toDomain()

        assertEquals("Alex", user.name)
        assertEquals("alex@example.com", user.email)
        assertEquals("https://example.com/alex.png", user.image)
        assertEquals(7, user.id)
    }

    @Test
    fun `user response dto maps users and total`() {
        val dto = TestData.userResponseDto(
            total = 42,
            users = listOf(
                TestData.userDto(id = 1, firstName = "One"),
                TestData.userDto(id = 2, firstName = "Two"),
            ),
        )

        val response = dto.toDomain()

        assertEquals(42, response.total)
        assertEquals(2, response.users.size)
        assertEquals("One", response.users[0].name)
        assertEquals("Two", response.users[1].name)
    }

    @Test
    fun `user response dto uses defaults when fields are null`() {
        val dto = TestData.userResponseDto(total = null, users = null)

        val response = dto.toDomain()

        assertEquals(0, response.total)
        assertTrue(response.users.isEmpty())
    }
}
