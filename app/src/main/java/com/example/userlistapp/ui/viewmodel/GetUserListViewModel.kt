package com.example.userlistapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.userlistapp.data.remote.RetrofitInstance
import com.example.userlistapp.data.repo.UserListRepoImpl
import com.example.userlistapp.domain.usecase.UserListUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GetUserListViewModel(
    private var userListUseCase: UserListUseCase = UserListUseCase(
        UserListRepoImpl(RetrofitInstance.getApiService())
    )
) : ViewModel() {
    private var _state = MutableStateFlow<UserListState>(UserListState(users = emptyList(), isLoading = true))
    val state = _state.asStateFlow()

    private var _effect = MutableSharedFlow<UserListEvent>()
    var effect = _effect.asSharedFlow()

    var currentPage = 1
    var limit = 20
    var hasMore = true
    var isLoadingMore  = false


    fun handleIntent(intent: UerListIntent) {
        when (intent) {
            is UerListIntent.LoadUser -> {
                loadUser()
            }
            is UerListIntent.LoadNext -> {
                loadNextPage()
            }
        }
    }

    private fun loadUser() {

        _state.value = UserListState(users = emptyList(), isLoading = true)

        viewModelScope.launch {
            try {
                val response = userListUseCase.getUserList(currentPage,limit)
                val newUser = response.users
                val total = response.total

                hasMore = total >  newUser.size
                currentPage++
                isLoadingMore = false

                _state.value = UserListState(users = newUser, isLoading = false)

            } catch (e: Exception) {
                _state.value = UserListState(users = emptyList(), isLoading = false)
                Log.e("UserListViewModel", "Error loading user list: ${e.message}", e)
                _effect.emit(UserListEvent.ShowError(e.message ?: "Unknown error"))
            }

        }

    }

    private fun loadNextPage() {
        if(isLoadingMore || !hasMore)
            return
        isLoadingMore = true
        _state.update{ it.copy(isLoadingMore = true,isLoading = false) }

        viewModelScope.launch {
            try {
                val response = userListUseCase.getUserList(currentPage,limit)
                val newUser = response.users
                val total = response.total
                val currentUsers= _state.value.users.orEmpty()

                val combined= newUser + currentUsers

                hasMore = total >  combined.size
                currentPage++
                isLoadingMore = false

                _state.update { it.copy(isLoadingMore = false, canLoadMore = hasMore, users = combined ) }

            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMore = false, canLoadMore = hasMore, ) }

                Log.e("UserListViewModel", "Error loading user list: ${e.message}", e)
                _effect.emit(UserListEvent.ShowError(e.message ?: "Unknown error"))
            }

        }

    }

}