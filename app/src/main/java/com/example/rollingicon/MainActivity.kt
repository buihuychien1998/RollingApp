package com.example.rollingicon

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RollingIconApp()
        }
    }

    fun startWallpaperService(iconsList: List<AppIcon>) {
        saveIconsToPreferences(iconsList)

        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(this, RollingIconWallpaperService::class.java)
        )
        startActivity(intent)
    }

    // Function to load icons from SharedPreferences
    fun loadIconsFromPreferences(): List<AppIcon> {
        val sharedPreferences = getSharedPreferences("icon_data", MODE_PRIVATE)
        val iconsJson = sharedPreferences.getString("icons_list", null)

        Log.d("RollingIcon", "Loaded icons: $iconsJson")

        // If no icons are stored, return an empty list
        if (iconsJson.isNullOrEmpty()) {
            return emptyList()
        }

        // Deserialize the JSON string into a list of AppIcon objects
        val gson = Gson()
        val iconType = object : TypeToken<List<AppIcon>>() {}.type
        return gson.fromJson(iconsJson, iconType)
    }

    // Function to save icons to SharedPreferences
    fun saveIconsToPreferences(iconsList: List<AppIcon>) {
        // Convert the icons list to JSON
        val gson = Gson()
        val iconsJson = gson.toJson(iconsList)

        // Save the icons list in SharedPreferences
        val sharedPreferences = getSharedPreferences("icon_data", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("icons_list", iconsJson)
            apply()
        }
    }
}

@Composable
fun RollingIconApp() {
    val context = LocalContext.current as MainActivity
    // State variable to hold the list of items
    val allInstalledApps = remember { mutableStateListOf<AppIcon>() }
    val appIcons = remember { mutableStateListOf<AppIcon>() }
    var loading by remember { mutableStateOf(true) } // Loading state

    // Load the icons from SharedPreferences asynchronously
    LaunchedEffect(Unit) {
        // Simulate asynchronous loading with delay (could be replaced with actual loading code)
        loading = true
        val iconsFromPreferences = withContext(Dispatchers.IO) {
            context.loadIconsFromPreferences()
        }
        // Load installed apps in the background
        val installedApps = withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            getInstalledApps(packageManager)
        }

        appIcons.clear() // Clear existing list
        appIcons.addAll(iconsFromPreferences) // Add loaded icons
        allInstalledApps.addAll(installedApps)
//        delay(300)  // Optional: Add a delay to simulate loading time
        loading = false
    }

    // Show loading indicator while fetching icons
//    Crossfade(targetState = loading, label = "") { isLoading ->
//        if (isLoading) {
//            LoadingScreen()
//        } else {
//            // Once loading is done, show the icons
//            MainContent(
//                context = context,
//                allInstalledApps = allInstalledApps,
//                currentAppIcons = appIcons,
//                onAddIcon = { appIcon -> appIcons.add(appIcon) },
//                onRemoveIcon = { appIcon -> appIcons.remove(appIcon) }
//            )
//        }
//    }
    // Loading screen with a spinner and a message
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (loading) {
            LoadingScreen()
        } else {
            MainContent(
                context = context,
                allInstalledApps = allInstalledApps,
                currentAppIcons = appIcons,
                onAddIcon = { appIcon -> appIcons.add(appIcon) },
                onRemoveIcon = { appIcon -> appIcons.remove(appIcon) }
            )
        }
    }

//    AnimatedContent(
//        targetState = loading,
//        transitionSpec = {
//            slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() togetherWith
//                    slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
//        }
//    ) { isLoading ->
//        if (isLoading) {
//            LoadingScreen()
//        } else {
//            MainContent(
//                context = context,
//                appIcons = appIcons,
//                onAddIcon = { appIcons.add(it) },
//                onRemoveIcon = { appIcons.remove(it) }
//            )
//        }
//    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading icons...",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator() // Spinner loading
    }
}

@Composable
fun MainContent(
    context: Context,
    allInstalledApps: MutableList<AppIcon>,
    currentAppIcons: MutableList<AppIcon>,
    onAddIcon: (AppIcon) -> Unit,
    onRemoveIcon: (AppIcon) -> Unit
) {
    AppIconList(
        context = context,
        allInstalledApps = allInstalledApps,
        currentAppIcons = currentAppIcons,
        onAddIcon = onAddIcon,
        onRemoveIcon = onRemoveIcon
    )
}


@Composable
fun AppIconList(
    context: Context,
    allInstalledApps: MutableList<AppIcon>,
    currentAppIcons: MutableList<AppIcon>,
    onAddIcon: (AppIcon) -> Unit,
    onRemoveIcon: (AppIcon) -> Unit
) {
    val buffer: Int = 1  // Buffer to load more items when we get near the end

    val displayedApps = remember { mutableStateListOf<AppIcon>() }
    // State to track the scroll position
    val smoothScrollState = rememberLazyListState()

    // Coroutine scope for handling background operations like loading data
    val coroutineScope = rememberCoroutineScope()

    val batchSize = 15 // Number of items to load per batch
    // State to track if more items are being loaded
    var isLoading by remember { mutableStateOf(false) }

    // Load the first batch of apps on initial composition
    LaunchedEffect(Unit) {
        displayedApps.addAll(allInstalledApps.take(batchSize)) // Load the first batch
    }

    // Load more apps as the user scrolls near the end
    LaunchedEffect(smoothScrollState) {
        snapshotFlow { smoothScrollState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty() && !isLoading) {
                    val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0
                    // Khi cuộn gần đến cuối, tải thêm dữ liệu
                    if (lastVisibleItemIndex >= displayedApps.size - buffer && displayedApps.size < allInstalledApps.size) {
                        isLoading = true
                        // Simulate network delay
                        delay(1000)
                        // Tải thêm batch dữ liệu
                        val nextBatch = allInstalledApps.drop(displayedApps.size).take(batchSize)
                        displayedApps += nextBatch
                        isLoading = false
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "App Icons",
                textAlign = TextAlign.Center,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                modifier = Modifier.wrapContentSize()
            )


            Button(
                onClick = {
                    (context as MainActivity).startWallpaperService(currentAppIcons)
                },
                modifier = Modifier.wrapContentSize()
            ) {
                Text(text = "Start")
            }
        }
        LazyColumn(state = smoothScrollState) {
            items(displayedApps) { appIcon ->
                AppIconRow(
                    appIcon = appIcon,
                    appIcons = currentAppIcons,
                    onAddIcon = onAddIcon,
                    onRemoveIcon = onRemoveIcon
                )
            }

            // Display a loading indicator when fetching more items
            if (isLoading && displayedApps.size < allInstalledApps.size) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun AppIconRow(
    appIcon: AppIcon,
    appIcons: MutableList<AppIcon>,
    onAddIcon: (AppIcon) -> Unit,
    onRemoveIcon: (AppIcon) -> Unit
) {
    var isAdded by remember { mutableStateOf(appIcons.contains(appIcon)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            appIcon.drawable?.toBitmap()?.asImageBitmap()?.let {
                Image(
                    bitmap = it, // Drawable icon
                    contentDescription = "App Icon",
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = appIcon.name,
                style = TextStyle(fontWeight = FontWeight.Medium),
                color = Color.Black
            )
        }

        IconButton(onClick = {
            if (isAdded) {
                onRemoveIcon(appIcon)
            } else {
                onAddIcon(appIcon)
            }
            isAdded = !isAdded
        }) {
            Icon(
                painter = painterResource(id = if (isAdded) android.R.drawable.ic_menu_delete else android.R.drawable.ic_input_add),
                contentDescription = if (isAdded) "Remove Icon" else "Add Icon",
                tint = Color(0xFF4169E1)
            )
        }
    }
}

@Preview
@Composable
fun PreviewAppIconList() {
    val context = LocalContext.current
    AppIconList(
        context = context,
        allInstalledApps = mutableListOf(),
        currentAppIcons = mutableListOf(),
        onAddIcon = {},
        onRemoveIcon = {}
    )
}