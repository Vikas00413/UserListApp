package com.example.userlistapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.userlistapp.domain.model.User
import com.example.userlistapp.ui.viewmodel.GetUserListViewModel
import com.example.userlistapp.ui.viewmodel.UerListIntent
import com.example.userlistapp.ui.viewmodel.UserListEvent
import com.example.userlistapp.ui.viewmodel.UserListState

@Composable
fun UserListScreen(
    modifier: Modifier = Modifier,
    userListViewModel: GetUserListViewModel = viewModel(),
    onUserClick: (User) -> Unit = {},
) {
    val uiState by userListViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= info.totalItemsCount - 3

        }.collect { nearEnd ->
            if (nearEnd) userListViewModel.handleIntent(UerListIntent.LoadNext)
        }
    }
    LaunchedEffect(userListViewModel) {

        userListViewModel.effect.collect { event ->
            when (event) {

                is UserListEvent.ShowError -> {
                    // Handle error event
                    Toast.makeText(
                        context,
                        (event as UserListEvent.ShowError).message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UserListEvent.GoToNext -> {
                    onUserClick(uiState.users.firstOrNull() ?: return@collect)
                }
            }
        }

    }


    LaunchedEffect(Unit) {
        userListViewModel.handleIntent(UerListIntent.LoadUser)
    }


    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
            }  else if(uiState.users.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {

                    UserListItem(
                        userList = uiState.users,
                        listState = listState,
                        onUserClick = onUserClick,
                    )
                    if (userListViewModel.isLoadingMore) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp).align(Alignment.BottomCenter),strokeWidth = 3.dp)
                    }
                }

            }


        }


    }


}


@Composable
fun UserListItem(
    userList: List<User>,
    listState: LazyListState,
    onUserClick: (User) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        items(userList.size, key = { userList[it].id }) { index ->
            val user = userList[index]
            UserListItem(user,   modifier = Modifier.clickable { onUserClick(user) },)
        }

    }
}

@Composable
private fun UserListItem(
    user: User,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = user.image,
                contentDescription = "${user.name} avatar",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}