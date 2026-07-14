package com.example.userlistapp.ui.viewmodel

import com.example.userlistapp.domain.model.User


data class UserListState(
    val users: List<User>,
    val isLoading: Boolean,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
)