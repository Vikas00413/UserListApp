package com.example.userlistapp.domain.usecase

import com.example.userlistapp.domain.model.UserListResponse
import com.example.userlistapp.domain.repo.UserListRepo

class UserListUseCase(private val userListRepo: UserListRepo) {
    suspend fun getUserList(currentPage: Int, limit: Int): UserListResponse {
        return userListRepo.getUserList(currentPage, limit)
    }

}