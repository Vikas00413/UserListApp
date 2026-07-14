package com.example.userlistapp.domain.repo

import com.example.userlistapp.domain.model.UserListResponse

interface UserListRepo {

    suspend fun getUserList(currentPage:Int, limit:Int) : UserListResponse
}