package com.example.userlistapp.data.remote

import com.example.userlistapp.data.dto.UserResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): UserResponseDTO

}