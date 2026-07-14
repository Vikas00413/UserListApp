package com.example.userlistapp

import com.example.userlistapp.data.dto.UserDTO
import com.example.userlistapp.data.dto.UserResponseDTO
import com.example.userlistapp.domain.model.User
import com.example.userlistapp.domain.model.UserListResponse

object TestData {
    fun userDto(
        id: Int = 1,
        firstName: String? = "Jane",
        email: String? = "jane@example.com",
        image: String? = "https://example.com/jane.png",
    ) = UserDTO(
        address = null,
        age = null,
        bank = null,
        birthDate = null,
        bloodGroup = null,
        company = null,
        crypto = null,
        ein = null,
        email = email,
        eyeColor = null,
        firstName = firstName,
        gender = null,
        hair = null,
        height = null,
        id = id,
        image = image,
        ip = null,
        lastName = null,
        macAddress = null,
        maidenName = null,
        password = null,
        phone = null,
        role = null,
        ssn = null,
        university = null,
        userAgent = null,
        username = null,
        weight = null,
    )

    fun user(
        id: Int = 1,
        name: String = "Jane",
        email: String = "jane@example.com",
        image: String = "https://example.com/jane.png",
    ) = User(name = name, email = email, image = image, id = id)

    fun userResponseDto(
        total: Int? = 100,
        users: List<UserDTO>? = listOf(userDto()),
    ) = UserResponseDTO(
        limit = 20,
        skip = 0,
        total = total,
        users = users,
    )

    fun userListResponse(
        total: Int = 100,
        users: List<User> = listOf(user()),
    ) = UserListResponse(total = total, users = users)
}
