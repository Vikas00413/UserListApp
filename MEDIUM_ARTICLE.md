# From Zero to Infinite Scroll: My First Android Project

**Subtitle:** How I built a Jetpack Compose user list app with manual pagination, MVVM, navigation, and image loading — without the Paging library.

---

This is my **first Medium article**.

A few weeks ago, I started learning Android. I didn’t want to just follow a tutorial and copy code. I wanted to build something small but real — something that fetches data from an API, shows it in a list, loads more when I scroll, and opens a detail screen when I tap a user.

That’s how **UserListApp** was born.

In this article, I’ll walk you through everything I built: pagination without Paging 3, ViewModel architecture, Compose UI, navigation, and one painful Coil mistake that kept images from loading.

If you’re learning Android, I hope this saves you some time.

---

## What the app does

The app shows a scrollable list of users. Each row displays:

- A profile image
- The user’s name
- Their email

When you scroll near the bottom, the next page loads automatically. When you tap a user, you go to a detail screen. Press back, and you return to the list.

Simple idea. Lots to learn underneath.

---

## Tech stack

| Layer        | Technology              |
|--------------|-------------------------|
| UI           | Jetpack Compose + Material 3 |
| Architecture | MVVM                    |
| Networking   | Retrofit + OkHttp       |
| Images       | Coil 3                  |
| Navigation   | Navigation Compose      |
| API          | DummyJSON (dummyjson.com) |
| Tests        | JUnit + Mockito         |

---

## How the project is organized

```
app/
 ├── data/        → API, DTOs, repository
 ├── domain/      → models, use cases
 ├── ui/          → screens & ViewModel
 └── navigation/  → routes & NavGraph
```

Keeping data, business logic, and UI separate made the app easier to reason about — and easier to test.

---

## Why I skipped the Paging library

Android has **Paging 3**. It’s excellent for production apps with large datasets.

But I was learning. I wanted to understand:

- How page numbers work
- When to trigger the next API call
- How to stop duplicate requests
- How scroll position connects to data loading

So I built pagination myself first.

> You don’t need a big library to understand infinite scroll. Sometimes building it manually is the best teacher.

---

# 1. Pagination: The API Layer

The DummyJSON API supports page-based requests.

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): UserResponseDTO
}
```

A typical response looks like this:

```json
{
  "users": [
    {
      "id": 1,
      "firstName": "Emily",
      "email": "emily.johnson@x.dummyjson.com",
      "image": "https://dummyjson.com/icon/emilys/128"
    }
  ],
  "total": 208,
  "limit": 20,
  "skip": 0
}
```

- `users` → current page
- `total` → how many users exist in total
- We compare loaded count vs `total` to know if more pages remain

The API response is mapped to a clean domain model:

```kotlin
data class User(
    val name: String,
    val email: String,
    val image: String,
    val id: Int
)

data class UserListResponse(
    val total: Int,
    val users: List<User>
)
```

---

# 2. ViewModel: Where the Logic Lives

The screen shouldn’t think. The **ViewModel** should.

I used a simple pattern:

```
UI sends Intent  →  ViewModel processes  →  UI observes State
                         ↓
                   One-time Effects (Toast, etc.)
```

---

## State — what the screen shows

```kotlin
data class UserListState(
    val users: List<User>,
    val isLoading: Boolean,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
)
```

The ViewModel exposes it as a `StateFlow`:

```kotlin
private val _state = MutableStateFlow(
    UserListState(users = emptyList(), isLoading = true)
)
val state = _state.asStateFlow()
```

The UI collects it:

```kotlin
val uiState by viewModel.state.collectAsStateWithLifecycle()
```

Any change to `_state` automatically updates the screen.

---

## Intent — what the UI asks for

Instead of calling many functions from the UI, I send **intents**:

```kotlin
sealed class UerListIntent {
    object LoadUser : UerListIntent()
    object LoadNext : UerListIntent()
}
```

- Screen opens → `LoadUser`
- User scrolls near bottom → `LoadNext`

```kotlin
fun handleIntent(intent: UerListIntent) {
    when (intent) {
        is UerListIntent.LoadUser -> loadUser()
        is UerListIntent.LoadNext -> loadNextPage()
    }
}
```

The UI says *what happened*. The ViewModel decides *what to do*.

---

## Effect — one-time events

Toasts and navigation shouldn’t live in state. For that I used **effects**:

```kotlin
sealed class UserListEvent {
    data class ShowError(val message: String) : UserListEvent()
}
```

```kotlin
LaunchedEffect(viewModel) {
    viewModel.effect.collect { event ->
        when (event) {
            is UserListEvent.ShowError -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

### State vs Effect

| State                    | Effect              |
|--------------------------|---------------------|
| Loading spinner visible  | Show error Toast    |
| User list updated        | Happens once        |
| Observed continuously    | Not stored in state |

---

## Pagination inside the ViewModel

```kotlin
var currentPage = 1
var limit = 20
var hasMore = true
var isLoadingMore = false
```

### Loading the first page

```kotlin
private fun loadUser() {
    _state.value = UserListState(users = emptyList(), isLoading = true)

    viewModelScope.launch {
        try {
            val response = userListUseCase.getUserList(currentPage, limit)
            val newUsers = response.users
            val total = response.total

            hasMore = total > newUsers.size
            currentPage++

            _state.value = UserListState(users = newUsers, isLoading = false)
        } catch (e: Exception) {
            _state.value = UserListState(users = emptyList(), isLoading = false)
            _effect.emit(UserListEvent.ShowError(e.message ?: "Unknown error"))
        }
    }
}
```

### Loading the next page

```kotlin
private fun loadNextPage() {
    if (isLoadingMore || !hasMore) return

    isLoadingMore = true
    _state.update { it.copy(isLoadingMore = true) }

    viewModelScope.launch {
        try {
            val response = userListUseCase.getUserList(currentPage, limit)
            val newUsers = response.users
            val total = response.total
            val combined = _state.value.users + newUsers

            hasMore = total > combined.size
            currentPage++
            isLoadingMore = false

            _state.update {
                it.copy(
                    isLoadingMore = false,
                    canLoadMore = hasMore,
                    users = combined
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingMore = false) }
            _effect.emit(UserListEvent.ShowError(e.message ?: "Unknown error"))
        }
    }
}
```

The most important line:

```kotlin
if (isLoadingMore || !hasMore) return
```

Without it, fast scrolling fires multiple API calls. I learned that the hard way.

---

# 3. UI: Building the List

## Detecting scroll near the bottom

This is where infinite scroll actually happens in the UI.

```kotlin
val listState = rememberLazyListState()

LaunchedEffect(listState) {
    snapshotFlow {
        val info = listState.layoutInfo
        val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisible >= info.totalItemsCount - 3
    }.collect { nearEnd ->
        if (nearEnd) {
            viewModel.handleIntent(UerListIntent.LoadNext)
        }
    }
}
```

`snapshotFlow` watches the scroll position. When the last visible item is within 3 rows of the end, we request the next page. Loading starts slightly early so the user doesn’t stare at a blank screen.

---

## Showing loading indicators

```kotlin
if (uiState.isLoading) {
    CircularProgressIndicator()
} else if (uiState.users.isNotEmpty()) {
    Box(Modifier.fillMaxSize()) {
        UserListContent(
            users = uiState.users,
            listState = listState,
            onUserClick = onUserClick,
        )
        if (viewModel.isLoadingMore) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
```

### Loading states

| State       | UI              |
|-------------|-----------------|
| First load  | Center spinner  |
| Next page   | Bottom spinner  |
| Data loaded | User list       |

---

## The LazyColumn

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = listState
) {
    items(userList.size, key = { userList[it].id }) { index ->
        val user = userList[index]
        UserListItem(
            user = user,
            modifier = Modifier.clickable { onUserClick(user) }
        )
    }
}
```

`LazyColumn` only composes visible items — perfect for paginated lists.

`key = { userList[it].id }` helps Compose track items correctly as the list grows.

---

## A single list item

```kotlin
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
```

Layout structure:

```
Card
 └── Row
      ├── AsyncImage  (avatar)
      ├── Spacer
      └── Column
           ├── Name
           └── Email
```

The item doesn’t navigate on its own. It calls `onUserClick(user)` and lets the parent decide what happens. Keeps the component reusable.

---

# 4. Navigation Between Screens

Once the list worked, I added a detail screen using **Navigation Compose**.

---

## Add dependency

```kotlin
// libs.versions.toml
navigation = "2.9.8"

androidx-navigation-compose = {
    group = "androidx.navigation",
    name = "navigation-compose",
    version.ref = "navigation"
}

// app/build.gradle.kts
implementation(libs.androidx.navigation.compose)
```

---

## Routes

```kotlin
object Routes {
    const val USER_LIST = "user_list"
    const val USER_DETAIL = "user_detail/{userId}"

    fun userDetail(userId: Int) = "user_detail/$userId"
}
```

### Routes table

| Route                    | Screen              |
|--------------------------|---------------------|
| `user_list`              | User list (start)   |
| `user_detail/{userId}`   | User detail screen  |

---

## NavGraph

```kotlin
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.USER_LIST,
        modifier = modifier,
    ) {
        composable(Routes.USER_LIST) {
            UserListScreen(
                onUserClick = { user ->
                    navController.navigate(Routes.userDetail(user.id))
                },
            )
        }

        composable(
            route = Routes.USER_DETAIL,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            ),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable

            UserDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
```

---

## MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserListAppTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
```

---

## Detail screen

```kotlin
@Composable
fun UserDetailScreen(
    userId: Int,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "User ID: $userId")
        }
    }
}
```

---

## Navigation flow

```
User taps list item
        ↓
onUserClick(user)
        ↓
navController.navigate("user_detail/5")
        ↓
UserDetailScreen opens
        ↓
userId read from route
        ↓
Back pressed → popBackStack()
        ↓
Returns to user list
```

---

## Who owns what?

| Responsibility     | Owner              |
|--------------------|--------------------|
| User list data     | ViewModel          |
| Pagination         | ViewModel          |
| Error Toast        | ViewModel Effect   |
| Screen switching   | NavController      |
| Back navigation    | popBackStack()     |

Don’t put `NavController` inside the ViewModel. Pass callbacks from the UI layer instead.

---

# 5. The Coil Mistake That Cost Me an Hour

Images weren’t showing. The API returned valid URLs. I had Coil installed. Still blank.

**Problem:** Coil 3 doesn’t load network images out of the box.

I only had:

```kotlin
implementation("io.coil-kt.coil3:coil-compose:3.5.0")
```

**Fix:**

```kotlin
implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")
```

One extra dependency. Problem solved.

Lesson: when something “should work” but doesn’t, check what the library expects you to add explicitly.

---

# 6. Unit Tests (Bonus)

I also added **11 unit tests** using **JUnit**, **Mockito**, and **kotlinx-coroutines-test**.

---

## Test dependencies

```kotlin
// libs.versions.toml
coroutines = "1.10.2"
mockito = "5.14.2"
mockitoKotlin = "5.4.0"

// app/build.gradle.kts
testImplementation(libs.junit)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.mockito.core)
testImplementation(libs.mockito.kotlin)
```

---

## All test cases (11 total)

| # | Test file | Test case name |
|---|-----------|----------------|
| 1 | DTOMapperTest | user dto maps to domain user |
| 2 | DTOMapperTest | user response dto maps users and total |
| 3 | DTOMapperTest | user response dto uses defaults when fields are null |
| 4 | UserListRepoImplTest | getUserList fetches from api and maps to domain |
| 5 | UserListUseCaseTest | getUserList delegates to repository |
| 6 | GetUserListViewModelTest | loadUser updates state with users on success |
| 7 | GetUserListViewModelTest | loadUser emits error effect on failure |
| 8 | GetUserListViewModelTest | loadNextPage appends users when more data is available |
| 9 | GetUserListViewModelTest | loadNextPage skips duplicate requests while loading |
| 10 | GetUserListViewModelTest | loadNextPage does nothing when no more pages |
| 11 | RoutesTest | userDetail builds route with user id |

---

## Example 1: DTO mapper test

Tests that API data maps correctly to domain models.

```kotlin
@Test
fun `user dto maps to domain user`() {
    val dto = TestData.userDto(
        id = 7,
        firstName = "Alex",
        email = "alex@example.com",
        image = "https://example.com/alex.png",
    )

    val user = dto.toDomain()

    assertEquals("Alex", user.name)
    assertEquals("alex@example.com", user.email)
    assertEquals("https://example.com/alex.png", user.image)
    assertEquals(7, user.id)
}
```

---

## Example 2: Repository test with Mockito

Mocks the API and verifies the repository calls it correctly.

```kotlin
@Test
fun `getUserList fetches from api and maps to domain`() = runTest {
    val dto = TestData.userResponseDto(total = 50)
    whenever(apiService.getUsers(page = eq(2), limit = eq(10))).thenReturn(dto)

    val result = repository.getUserList(currentPage = 2, limit = 10)

    verify(apiService).getUsers(page = eq(2), limit = eq(10))
    assertEquals(50, result.total)
    assertEquals(1, result.users.size)
}
```

---

## Example 3: ViewModel test — load success

Mocks the use case and checks ViewModel state updates.

```kotlin
@Test
fun `loadUser updates state with users on success`() = runTest(testDispatcher) {
  whenever(useCase.getUserList(currentPage = eq(1), limit = eq(20))).thenReturn(
      TestData.userListResponse(total = 100, users = users),
  )

  viewModel.handleIntent(UerListIntent.LoadUser)
  advanceUntilIdle()

  val state = viewModel.state.value
  assertFalse(state.isLoading)
  assertEquals(users, state.users)
  assertTrue(viewModel.hasMore)
  assertEquals(2, viewModel.currentPage)
}
```

---

## Example 4: ViewModel test — pagination guard

Ensures duplicate API calls are blocked while loading.

```kotlin
@Test
fun `loadNextPage skips duplicate requests while loading`() = runTest(testDispatcher) {
    viewModel.handleIntent(UerListIntent.LoadNext)
    viewModel.handleIntent(UerListIntent.LoadNext)  // second call ignored

    advanceUntilIdle()

    verify(useCase, times(1)).getUserList(currentPage = eq(2), limit = eq(20))
}
```

---

## Example 5: Navigation route test

```kotlin
@Test
fun `userDetail builds route with user id`() {
    assertEquals("user_detail/42", Routes.userDetail(userId = 42))
}
```

---

## Run all tests

```bash
./gradlew :app:testDebugUnitTest
```

All 11 tests pass.

---

## What testing taught me

1. ViewModel logic can be tested without running the app
2. Mockito makes it easy to fake API responses
3. `StandardTestDispatcher` is needed to test coroutines in ViewModel
4. Pagination guard (`isLoadingMore`) is worth testing — it prevents real bugs

---

# 7. The Full App Flow

```
App opens
    ↓
UserListScreen loads page 1
    ↓
LazyColumn shows users
    ↓
User scrolls near bottom
    ↓
ViewModel fetches next page
    ↓
New users appended to list
    ↓
User taps a row
    ↓
Detail screen opens
    ↓
Back returns to list
```

---

## What I learned

1. Pagination is mostly **state management** — page number, loading flags, hasMore.
2. `snapshotFlow` + `LazyColumn` is a clean way to trigger infinite scroll in Compose.
3. Guard conditions matter — `if (isLoadingMore || !hasMore) return` prevents chaos.
4. Separate **State** (what to show) from **Effect** (what happens once).
5. Keep navigation in the NavGraph, not inside list items.
6. Read library docs carefully — Coil 3 taught me that.
7. Building without Paging 3 first gave me real intuition for what libraries abstract away.

---

## What’s next

- Show full user info on the detail screen
- Pull-to-refresh
- Better error UI
- Try Paging 3 and compare both approaches

---

## Final thoughts

You don’t need a heavyweight library to build a working paginated list in Android.

With a page-based API, a ViewModel tracking state, `LazyColumn` watching scroll position, and Navigation Compose for screen transitions — you can build something that feels like a real app.

This was my first Android project. Writing about it helped me understand it better than reading ten tutorials.

If you’re starting out: pick one small app, break it into layers, and build it piece by piece. You’ll learn more than you expect.

---

## Before you publish on Medium

Add these screenshots:

1. User list screen
2. Detail screen after tapping a user
3. Bottom loading indicator while paginating
4. Architecture diagram (optional)
5. Before/after Coil image fix (optional)

Add your links:

- GitHub repo: (add your link here)
- LinkedIn: (add your link here)
- Twitter/X: (add your link here)

---

## Suggested Medium tags

Android, Jetpack Compose, Kotlin, MVVM, Pagination, Retrofit, Mobile Development, Navigation Compose

---

## Short promo for LinkedIn / X

Published my first Medium article!

Built an Android User List app with Jetpack Compose — manual pagination, MVVM, Navigation Compose, and a Coil 3 lesson learned the hard way.

Read it here: (add your Medium link)

---

## Alternative titles (pick one if you want to change)

1. From Zero to Infinite Scroll: My First Android Project
2. I Didn’t Use the Paging Library — And My Android App Still Works
3. Manual Pagination in Jetpack Compose — No Paging 3 Required
4. My First Android App: Lists, Pagination, and Lessons Learned
5. Scroll, Load, Repeat: Building Pagination in Android the Manual Way
