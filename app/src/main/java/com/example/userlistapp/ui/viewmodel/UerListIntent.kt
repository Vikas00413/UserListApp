package com.example.userlistapp.ui.viewmodel


sealed class UerListIntent {
    object LoadUser: UerListIntent()
    object LoadNext: UerListIntent()
}