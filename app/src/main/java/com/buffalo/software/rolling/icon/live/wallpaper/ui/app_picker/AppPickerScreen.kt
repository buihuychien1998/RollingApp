package com.buffalo.software.rolling.icon.live.wallpaper.ui.app_picker

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.buffalo.software.rolling.icon.live.wallpaper.R
import com.buffalo.software.rolling.icon.live.wallpaper.models.AppIcon
import com.buffalo.software.rolling.icon.live.wallpaper.theme.AppFont
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_C2D8FF
import com.buffalo.software.rolling.icon.live.wallpaper.theme.clr_D5DEE8
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.AppOpenAdController
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.BannerAd
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.InterstitialAdManager
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.banner_all
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.inter_done
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking.FirebaseAnalyticsEvents
import com.buffalo.software.rolling.icon.live.wallpaper.ui.ads.tracking.FirebaseEventLogger
import com.buffalo.software.rolling.icon.live.wallpaper.ui.loading.LoadingScreen
import com.buffalo.software.rolling.icon.live.wallpaper.ui.share_view_model.SharedViewModel
import com.buffalo.software.rolling.icon.live.wallpaper.utils.SHOW_AD
import com.buffalo.software.rolling.icon.live.wallpaper.utils.custom.SafeClick

@Composable
fun AppPickerScreen(
    navController: NavController,
    viewModel: AppPickerViewModel = viewModel()
) {
    val loading = viewModel.loading.collectAsState(false)
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        FirebaseEventLogger.trackScreenView(
            context,
            FirebaseAnalyticsEvents.SCREEN_ADD_APPLICATION_VIEW
        )

        activity?.let { act ->
            InterstitialAdManager.loadAd(act, inter_done)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.bg_rolling_app),
                contentDescription = "background_image",
                contentScale = ContentScale.FillBounds
            )

            AppIconList(
                viewModel = viewModel,
                navController = navController,
            )
        }

        if (loading.value) {
            LoadingScreen()
        }
    }
}

@Composable
fun AppIconList(
    viewModel: AppPickerViewModel,
    navController: NavController
) {
    val shareViewModel: SharedViewModel = viewModel(LocalContext.current as ComponentActivity)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val selectedApps by viewModel.selectedAppIcons.collectAsState()
    val initialSelectedApps by viewModel.initialSelectedApps.collectAsState()
    // Detect if the selected apps have changed
    val isChanged = selectedApps != initialSelectedApps
    val context = LocalContext.current
    val activity = context as? Activity

    // Back handler to detect back press and save changes
    BackHandler {
        AppOpenAdController.disableByClickAction = true
        onBack(isChanged, viewModel, selectedApps, shareViewModel, navController, activity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        //Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            AppPickerHeader(isChanged, viewModel, selectedApps, shareViewModel, navController)
        }
        if (SHOW_AD && !filteredApps.isNullOrEmpty()) {
            BannerAd(banner_all)
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Search Bar
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            TextField(
                value = searchQuery,
                onValueChange = { query ->
                    if (query.isNotEmpty() && searchQuery.isEmpty()) {
                        // Track only when user starts typing
                        FirebaseEventLogger.trackButtonClick(
                            context,
                            FirebaseAnalyticsEvents.CLICK_SEARCH
                        )
                    }
                    viewModel.updateSearchQuery(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp)) // Apply rounded corners
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp)),
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_seach),
                        modifier = Modifier.size(16.dp),
                        contentDescription = null
                    )
                },// Add white border with rounded corners
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.text_search),
                        fontFamily = AppFont.Grandstander,
                        fontWeight = FontWeight.Medium,
                        color = clr_D5DEE8,
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent, // Set background to transparent
                    unfocusedContainerColor = Color.Transparent, // Set background to transparent
                    focusedIndicatorColor = Color.Transparent, // Optionally hide the focus indicator
                    unfocusedIndicatorColor = Color.Transparent // Hide indicator when unfocused
                ),
                textStyle = TextStyle(color = Color.White), // Optionally set the text color
                singleLine = true
            )
            if (filteredApps?.isEmpty() == true) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(id = R.string.text_your_list_is_empty),
                        fontSize = 24.sp,
                        color = clr_C2D8FF,
                        fontFamily = AppFont.Grandstander,
                        fontWeight = FontWeight.Bold
                    )

                    Image(
                        painter = painterResource(id = R.drawable.img_no_content),
                        modifier = Modifier.fillMaxWidth(),
                        contentDescription = null
                    )
                }
            } else {
                LazyVerticalGrid(
                    state = rememberLazyGridState(),
                    columns = GridCells.Fixed(4), // 3 columns in the grid
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    items(
                        items = filteredApps ?: mutableListOf(),
                    ) { appIcon ->
                        AppIconGridItem(appIcon = appIcon,
                            isSelected = selectedApps.contains(appIcon),
                            onSelect = { viewModel.addIcon(appIcon) },
                            onDeselect = { viewModel.removeIcon(appIcon) }
                        )
                    }
                }
            }
        }


    }
}

@Composable
private fun AppPickerHeader(
    isChanged: Boolean,
    viewModel: AppPickerViewModel,
    selectedApps: MutableList<AppIcon>,
    shareViewModel: SharedViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        SafeClick(onClick = {
            AppOpenAdController.disableByClickAction = true
            onBack(isChanged, viewModel, selectedApps, shareViewModel, navController, activity)
        }) { enabled, onClick ->
            IconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .offset(x = (-16).dp),
            ) {
                Image(
                    modifier = Modifier
                        .size(24.dp),
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "ic_arrow_left"
                )
            }
        }

        Text(
            text = stringResource(id = R.string.text_add_application),
            textAlign = TextAlign.Start,
            fontFamily = AppFont.Grandstander,
            color = Color.White,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
            modifier = Modifier
                .weight(1f)
                .offset(x = (-12).dp),
        )


        SafeClick(onClick = {
            FirebaseEventLogger.trackButtonClick(
                context,
                FirebaseAnalyticsEvents.CLICK_CLEAR_APPLICATION
            )
            viewModel.clearSelectedIcons()
        }) { enabled, onClick ->
            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent,
                ),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = stringResource(id = R.string.text_clear),
                    fontFamily = AppFont.Grandstander,
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
                )
            }
        }

    }
}

fun onBack(
    isChanged: Boolean,
    viewModel: AppPickerViewModel,
    selectedApps: MutableList<AppIcon>,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    activity: Activity?
) {
    val navigateBack = {
        AppOpenAdController.disableByClickAction = true
        navController.popBackStack()
    }

    if (isChanged) {
        viewModel.saveSelectedIcons(selectedApps, onSuccess = {
            sharedViewModel.setIconsChanged(true)

            activity?.let {
                InterstitialAdManager.showAd(it, inter_done) {
                    navigateBack()
                }
            } ?: navigateBack()

        }, onFailure = { error ->
            Log.e("onBack", "Error saving icons: ${error.message}")
        })
    } else {
        sharedViewModel.setIconsChanged(false)

        activity?.let {
            InterstitialAdManager.showAd(it, inter_done) {
                navigateBack()
            }
        } ?: navigateBack()
    }
}

@Composable
fun AppIconGridItem(
    appIcon: AppIcon, isSelected: Boolean, onSelect: () -> Unit, onDeselect: () -> Unit
) {
    // Use remember to avoid recalculating the bitmap on recomposition
    val appIconBitmap by remember {
        mutableStateOf(appIcon.appBitmap)
    }

    Box(modifier = Modifier
        .size(80.dp)
        .clickable {
            if (isSelected) onDeselect() else onSelect()
        }
        .background(
            color = Color.Transparent,
            shape = RoundedCornerShape(12.dp) // Adjusted for smaller size
        ), contentAlignment = Alignment.Center) {
        appIconBitmap?.let {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(it)
//                    .build(),
//                contentDescription = "App Icon",
//                placeholder = painterResource(id = R.drawable.ic_place_holder), // Replace with your placeholder resource
//                modifier = Modifier
//                    .size(60.dp)
//                    .clip(RoundedCornerShape(12.dp)),
//                contentScale = ContentScale.Crop
//            )
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)) // Apply rounded corners
                ,// Debugging layout
                contentScale = ContentScale.Crop
            )
        }


        // Checkmark overlay
        if (isSelected) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Selected",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp) // Icon size for the checkmark
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppIconList() {
    AppPickerScreen(NavController(LocalContext.current))
}