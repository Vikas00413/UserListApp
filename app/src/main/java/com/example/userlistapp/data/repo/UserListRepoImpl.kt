package com.example.userlistapp.data.repo

import com.example.userlistapp.data.remote.ApiService
import com.example.userlistapp.data.toDomain
import com.example.userlistapp.domain.model.User
import com.example.userlistapp.domain.model.UserListResponse
import com.example.userlistapp.domain.repo.UserListRepo

class UserListRepoImpl(private val apiService: ApiService) : UserListRepo {


    override suspend fun getUserList(
        currentPage: Int,
        limit: Int
    ): UserListResponse {
        return apiService.getUsers(currentPage, limit).toDomain()
    }
}