package com.example.userlistapp.ui.viewmodel



sealed class UserListEvent {
    data class ShowError(val message: String) : UserListEvent()
    data class GoToNext(val message: String) : UserListEvent()

}