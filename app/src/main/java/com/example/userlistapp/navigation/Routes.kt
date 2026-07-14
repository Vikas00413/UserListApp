package com.example.userlistapp.navigation

object Routes {
    const val USER_LIST = "user_list"
    const val USER_DETAIL = "user_detail/{userId}"

    fun userDetail(userId: Int) = "user_detail/$userId"
}
