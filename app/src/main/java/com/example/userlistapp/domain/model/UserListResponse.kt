package com.example.userlistapp.domain.model

data class UserListResponse(
    var total: Int,
    var users: List<User>
)