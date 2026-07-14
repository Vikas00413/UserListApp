package com.example.userlistapp.data

import com.example.userlistapp.data.dto.UserDTO
import com.example.userlistapp.data.dto.UserResponseDTO
import com.example.userlistapp.domain.model.User
import com.example.userlistapp.domain.model.UserListResponse


fun UserDTO.toDomain(): User {
    return User(this.firstName.toString(), this.email.toString(), this.image.toString(), this.id)
}

fun UserResponseDTO.toDomain(): UserListResponse{
    val users = this.users?.map { it.toDomain() } ?: emptyList()
    return UserListResponse(this.total ?: 0, users)
}