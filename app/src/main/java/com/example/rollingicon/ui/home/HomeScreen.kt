package com.example.rollingicon.ui.home

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rollingicon.R
import com.example.rollingicon.models.AppIcon
import com.example.rollingicon.routes.AppRoutes
import com.example.rollingicon.theme.AppFont
import com.example.rollingicon.theme.clr_4664FF
import com.example.rollingicon.theme.clr_C2D8FF
import com.example.rollingicon.ui.ads.BannerAd
import com.example.rollingicon.ui.ads.banner_all
import com.example.rollingicon.ui.dialog.SuccessDialog
import com.example.rollingicon.ui.loading.LoadingScreen
import com.example.rollingicon.ui.share_view_model.SharedViewModel
import com.example.rollingicon.utils.IconType
import com.example.rollingicon.utils.PermissionUtils
import com.example.rollingicon.utils.custom.SafeClick
import com.example.rollingicon.utils.startWallpaperService
import com.example.rollingicon.utils.toBitmap


@Composable
fun HomeScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val viewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    val appIcons by remember { derivedStateOf { viewModel.appIcons } }
    val loading by remember { derivedStateOf { viewModel.loading } }
    val iconsChanged by sharedViewModel.iconsChanged.collectAsState()

    // Refresh the data when the screen regains focus
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Trigger refresh if the icons have changed and the screen is resuming
                println("LifecycleEventObserver")
                println("$iconsChanged")
                if (iconsChanged) {
                    viewModel.loadAppIcons() // Reload the data
                    sharedViewModel.setIconsChanged(false) // Reset the flag
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            val deniedPermissions = permissions.filter { !it.value }.keys.toList()

            if (deniedPermissions.isEmpty()) {
                navController.navigate(viewModel.clickedRoutes.route)
//                PermissionUtils.onPermissionsGranted(context)
            } else {
                if (context is Activity) {

                    val permanentlyDenied = deniedPermissions.filter {
                        !ActivityCompat.shouldShowRequestPermissionRationale(context, it)
                    }

                    if (permanentlyDenied.isNotEmpty()) {
                        PermissionUtils.onPermissionsPermanentlyDenied(context, permanentlyDenied)
                    } else {
                        PermissionUtils.onPermissionsDenied(context, deniedPermissions)
                    }
                }

            }
        }

    // PermissionUtils instance
    val permissionUtils = remember {

        PermissionUtils(
            context = context,
            requestPermissionLauncher = requestPermissionLauncher
        )
    }

    // Loading screen with a spinner and a message
    Surface(modifier = Modifier.fillMaxSize()) {
        RollingIconScreen(
            navController = navController,
            appIcons = appIcons.value?.toMutableList(),
            onAddApplication = {
                // Handle adding application
                viewModel.clickedRoutes = AppRoutes.AppPicker
                navController.navigate(AppRoutes.AppPicker.route)
            },
            onAddPhotos = {
                // Handle adding photos
                viewModel.clickedRoutes = AppRoutes.ImagePicker
                permissionUtils.requestStoragePermissions()

            },
            onAddVideos = {
                // Handle adding videos
                viewModel.clickedRoutes = AppRoutes.VideoPicker
                permissionUtils.requestStoragePermissions()
            }
        )
        if (loading) {
            LoadingScreen()
        }
    }

}

@Composable
fun RollingIconScreen(
    navController: NavController,
    appIcons: MutableList<AppIcon>?,
    onAddApplication: () -> Unit,
    onAddPhotos: () -> Unit,
    onAddVideos: () -> Unit
) {
    val showCreateDialog = remember { mutableStateOf(false) }
    val isWallpaperSet = remember { mutableStateOf(false) }
    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    val liveWallpaperLauncher: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                isWallpaperSet.value = true
            }
        }


    Surface(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg_rolling_app),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header with title and toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                HomeHeader(navController, liveWallpaperLauncher, appIcons)
            }
            BannerAd(banner_all)

            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AddIconSection(showCreateDialog, appIcons)
                Spacer(modifier = Modifier.height(4.dp))
                AppIconsGrid(appIcons)
            }
        }

        // Bottom sheet dialog for adding options
        if (showCreateDialog.value) {
            AddIconDialog(
                onAddApplication = {
                    onAddApplication()
                    showCreateDialog.value = false
                },
                onAddPhotos = {
                    onAddPhotos()
                    showCreateDialog.value = false
                },
                onAddVideos = {
                    onAddVideos()
                    showCreateDialog.value = false
                },
                onDismiss = {
                    showCreateDialog.value = false
                }
            )
        }

        if (isWallpaperSet.value) {
            SuccessDialog(
                onDismiss = {
                    isWallpaperSet.value = false
                }
            )
        }
    }
}

@Composable
private fun AppIconsGrid(appIcons: List<AppIcon>?) {
    if (appIcons?.isEmpty() == true) {
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
            columns = GridCells.Fixed(4), // 3 columns in the grid
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 12.dp)

        ) {
            appIcons?.let {
                items(it.drop(4)) { appIcon ->
                    AppIconItem(appIcon)
                }
            }
        }
    }

}

@Composable
private fun AddIconSection(
    showDialog: MutableState<Boolean>,
    appIcons: MutableList<AppIcon>?
) {
    if (appIcons == null || appIcons.isEmpty()) {
        // Show only the Add Icon Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDialog.value = true
                }
        ) {
            Image(
                painter = painterResource(R.drawable.img_add_media),
                contentDescription = "Add Icon Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_add_icon),
                    contentDescription = "Add Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.text_add_icon),
                    fontFamily = AppFont.Grandstander,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    } else {
        // Show Add Icon Button and Grid
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Add Icon Section (Button)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDialog.value = true
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.bg_add_icon),
                    contentDescription = "Add Icon Background",
                    modifier = Modifier
                        .wrapContentSize()
                        .height(176.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_add_icon),
                        contentDescription = "Add Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.text_add_icon),
                        fontFamily = AppFont.Grandstander,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            // First Grid (2 items in the first row) aligned next to the button
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 columns for first row
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(176.dp)
            ) {
                items(appIcons.take(4)) { appIcon ->
                    AppIconItem(appIcon)
                }
            }
        }
    }
}


@Composable
private fun AppIconItem(appIcon: AppIcon) {
    // Use remember to avoid recalculating the bitmap on recomposition
    val appIconBitmap by remember(appIcon.drawable) {
        mutableStateOf(appIcon.drawable?.toBitmap())
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp) // Adjusted for smaller size
            ), contentAlignment = Alignment.Center
    ) {
        appIconBitmap?.let {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(it)
//                    .build(),
//                contentDescription = "App Icon",
//                placeholder = painterResource(id = R.drawable.ic_place_holder), // Replace with your placeholder resource
//                modifier = Modifier
//                    .size(60.dp)
//                    .clip(RoundedCornerShape(12.dp)) // Apply rounded corners
//                ,// Debugging layout
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

        if (appIcon.type == IconType.VIDEO.name) Image(
            painter = painterResource(R.drawable.ic_play_video),
            contentDescription = "Video Play",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun HomeHeader(
    navController: NavController,
    launcher: ActivityResultLauncher<Intent>,
    appIcons: MutableList<AppIcon>?
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier
                .weight(1f),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFont.Grandstander
        )
        Spacer(modifier = Modifier.width(4.dp))
        SafeClick(onClick = { navController.navigate(AppRoutes.Settings.route) }) { enabled, onClick ->
            IconButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_settings),
                    modifier = Modifier
                        .size(32.dp),
                    contentDescription = "Settings",
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        SafeClick(onClick = {
            try {
                if (appIcons.isNullOrEmpty()) {
                    Toast.makeText(context, R.string.text_no_icons_selected, Toast.LENGTH_SHORT)
                        .show()
                    return@SafeClick
                }
                context.startWallpaperService(launcher = launcher)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }) { enabled, onClick ->
            Button(
                colors = ButtonDefaults.buttonColors(
                    contentColor = clr_4664FF,
                    containerColor = Color.White,
                    disabledContainerColor = Color.White,
                    disabledContentColor = clr_4664FF,
                ),
                shape = RoundedCornerShape(20),
                contentPadding = PaddingValues(horizontal = 8.dp),
                onClick = onClick,
                enabled = enabled
            ) {
                Text(
                    text = stringResource(id = R.string.text_preview),
                    fontSize = 14.sp,
                    fontFamily = AppFont.Grandstander
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_preview),
                    modifier = Modifier
                        .size(24.dp),
                    contentDescription = "Preview",
                )
            }
        }
    }
}

@Composable
fun AddIconDialog(
    onAddApplication: () -> Unit,
    onAddPhotos: () -> Unit,
    onAddVideos: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.Start),
                    text = stringResource(id = R.string.text_add_icon),
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontFamily = AppFont.Grandstander,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Add Application button
                SafeClick(
                    onClick = onAddApplication,
                    content = { enabled, onClick ->
                        Button(
                            onClick = onClick,  // Trigger SafeClick's internal onClick when clicked
                            enabled = enabled,  // Disable the button if not enabled
                            shape = RoundedCornerShape(20),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = clr_4664FF
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.ic_add_application),
                                    contentDescription = stringResource(id = R.string.text_add_application)
                                )
                                Text(
                                    text = stringResource(id = R.string.text_add_application),
                                    fontFamily = AppFont.Grandstander,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                )


                Row {
                    // Add Photos button
                    SafeClick(
                        onClick = onAddVideos,
                        content = { enabled, onClick ->
                            Button(
                                onClick = onClick,  // Trigger SafeClick's internal onClick when clicked
                                enabled = enabled,  // Disable the button if not enabled
                                shape = RoundedCornerShape(20),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = clr_C2D8FF,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(R.drawable.ic_add_video),
                                        contentDescription = stringResource(id = R.string.text_add_videos)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.text_add_videos),
                                        fontFamily = AppFont.Grandstander,
                                        fontSize = 16.sp,
                                        color = clr_4664FF
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Add Videos button

                    SafeClick(
                        onClick = onAddPhotos,
                        content = { enabled, onClick ->
                            Button(
                                onClick = onClick,  // Trigger SafeClick's internal onClick when clicked
                                enabled = enabled,  // Disable the button if not enabled
                                shape = RoundedCornerShape(20),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = clr_C2D8FF,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(R.drawable.ic_add_photo),
                                        contentDescription = stringResource(id = R.string.text_add_photos)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.text_add_photos),
                                        fontFamily = AppFont.Grandstander,
                                        fontSize = 16.sp,
                                        color = clr_4664FF
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HomePreview() {

    RollingIconScreen(
        navController = NavController(LocalContext.current),
        appIcons = mutableListOf(),
        onAddApplication = {
            // Handle adding application
        },
        onAddPhotos = {
            // Handle adding photos
        },
        onAddVideos = {
            // Handle adding videos
        }
    )
}
