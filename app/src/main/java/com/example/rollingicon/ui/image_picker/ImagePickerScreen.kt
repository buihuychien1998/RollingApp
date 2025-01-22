package com.example.rollingicon.ui.image_picker

import android.content.ClipData
import android.content.ClipDescription
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import coil3.compose.rememberAsyncImagePainter
import com.example.rollingicon.R
import com.example.rollingicon.models.AppIcon
import com.example.rollingicon.theme.AppFont
import com.example.rollingicon.theme.clr_C2D8FF
import com.example.rollingicon.theme.clr_FFDDDB
import com.example.rollingicon.ui.loading.LoadingScreen
import com.example.rollingicon.ui.share_view_model.SharedViewModel
import com.example.rollingicon.utils.IconType
import com.example.rollingicon.utils.custom.SafeClick
import com.example.rollingicon.utils.custom.SafeClickable
import com.example.rollingicon.utils.getCompressedBitmapFromUri
import com.example.rollingicon.utils.toBitmap


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePickerScreen(
    navController: NavController,
    viewModel: ImagePickerViewModel = viewModel(),
    sharedViewModel: SharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val loading = viewModel.loading.collectAsState(false)
    val selectedMedia by viewModel.selectedImage.collectAsState()
    val initialSelectedApps by viewModel.initialSelectedApps.collectAsState()

    val isChanged = selectedMedia != initialSelectedApps

    var dragBoxIndex by remember {
        mutableIntStateOf(0)
    }
    var isDraggingOverDeleteZone by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    val screenHeightPx =
        LocalDensity.current.run { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    // Back handler to detect back press and save changes
    BackHandler {
        onBack(
            isChanged,
            viewModel,
            selectedMedia ?: mutableListOf(),
            sharedViewModel,
            navController
        ) // Navigate back
    }

    val pickMediaLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments(),
            onResult = { uris ->
                uris.forEach { uri ->
                    viewModel.addMedia(uri) // Use ViewModel to add media
                }
            })

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberAsyncImagePainter(R.drawable.bg_rolling_app),
                contentDescription = "background_image",
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SafeClick(onClick = { onBack(
                        isChanged,
                        viewModel,
                        selectedMedia ?: mutableListOf(),
                        sharedViewModel,
                        navController
                    ) }) { enabled, onClick ->
                        IconButton(
                            onClick = onClick,
                            enabled = enabled,
                            modifier = Modifier
                                .offset(x = (-16).dp),
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(24.dp),
                                painter = rememberAsyncImagePainter(R.drawable.ic_arrow_left),
                                contentDescription = "ic_arrow_left"
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.text_add_photos),
                        textAlign = TextAlign.Start,
                        fontFamily = AppFont.Grandstander,
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                        modifier = Modifier
                            .weight(1f)
                            .offset(x = (-12).dp),
                    )


                    SafeClick(onClick = { viewModel.clearSelection() }) { enabled, onClick ->
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
                Spacer(modifier = Modifier.height(16.dp))

                SafeClickable(
                    onClick = {
                        pickMediaLauncher.launch(
                            arrayOf(
                                "image/*"
                            )
                        )
                    },
                    content = {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {

                            Image(
                                painter = rememberAsyncImagePainter(R.drawable.img_add_media),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = rememberAsyncImagePainter(R.drawable.ic_add_icon),
                                    contentDescription = "Add Icon",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.text_add_photos),
                                    fontFamily = AppFont.Grandstander,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))

                if (selectedMedia?.isEmpty() == true) {
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
                        columns = GridCells.Fixed(4), // 4 columns in the grid
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    event
                                        .mimeTypes()
                                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                },
                                target = remember {
                                    object : DragAndDropTarget {
                                        override fun onDrop(event: DragAndDropEvent) = true

                                        override fun onEnded(event: DragAndDropEvent) {
                                            // When the drag event stops
                                            val text = event.toAndroidDragEvent().clipData
                                                ?.getItemAt(0)?.text.toString()

                                            println("Drag data was $text")
                                            // If dragged to bottom, handle the drop (e.g., remove item)
                                            val targetPosition =
                                                event.toAndroidDragEvent().y // You can check the Y position here
                                            println("Item dropped at the bottom of the screen - removing it. $targetPosition")
                                            println("Item dropped at the bottom of the screen - removing it. ${screenHeightPx * 0.8f}")

                                            isDragging = false
                                            isDraggingOverDeleteZone = false
                                        }

                                        override fun onMoved(event: DragAndDropEvent) {
                                            val targetPosition = event.toAndroidDragEvent().y
                                            isDraggingOverDeleteZone =
                                                targetPosition > screenHeightPx * 0.8f
                                            println("onChanged $targetPosition")
                                            if (isDraggingOverDeleteZone && dragBoxIndex != -1) {
                                                println("Item dropped at the bottom of the screen - removing it. $dragBoxIndex")
                                                // You can remove the item here if the target is at the bottom
                                                isDragging = false
                                                isDraggingOverDeleteZone = false
                                                viewModel.deleteItem(dragBoxIndex)
                                                dragBoxIndex = -1
                                            }
                                        }

                                        override fun onStarted(event: DragAndDropEvent) {

                                        }

                                    }
                                }
                            ),
                    ) {
                        selectedMedia?.let { list ->
                            items(list.size) { index ->
                                val appIcon = list[index]
                                // Use remember to avoid recalculating the bitmap on recomposition
                                val appIconBitmap by remember(appIcon.drawable) {
                                    mutableStateOf(appIcon.drawable?.toBitmap() ?: context.getCompressedBitmapFromUri(Uri.parse(appIcon.filePath)))
                                }

                                Box(contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .dragAndDropSource(
                                            drawDragDecoration = {
                                                // Draw the bitmap or actual content of the dragged item
                                                appIconBitmap?.let { bitmap ->
                                                    drawImage(
                                                        image = bitmap.asImageBitmap(),
                                                        topLeft = Offset.Zero,
                                                        alpha = 0.9f // Adjust transparency for the decoration
                                                    )
                                                }
                                            }
                                        ) {
                                            detectTapGestures(
                                                onTap = {
                                                    viewModel.toggleSelection(index)
                                                },
                                                onLongPress = {
                                                    dragBoxIndex = index
                                                    isDragging = true
                                                    startTransfer(
                                                        transferData = DragAndDropTransferData(
                                                            clipData = ClipData.newPlainText(
                                                                "text",
                                                                "$index"
                                                            )
                                                        )
                                                    )

                                                }
                                            )
                                        }
                                ) {
                                    when (appIcon.type) {
                                        IconType.IMAGE.name -> {
                                            // Display image
                                            if (appIcon.drawable == null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        Uri.parse(
                                                            appIcon.filePath
                                                        )
                                                    ),
                                                    contentDescription = "Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                )
                                            } else {
                                                appIconBitmap?.let {
//                                                    AsyncImage(
//                                                        model = ImageRequest.Builder(
//                                                            LocalContext.current
//                                                        )
//                                                            .data(it)
//                                                            .crossfade(true)
//                                                            .build(),
//                                                        contentDescription = "App Icon",
//                                                        placeholder = painterResource(id = R.drawable.ic_place_holder), // Replace with your placeholder resource
//                                                        modifier = Modifier
//                                                            .size(60.dp)
//                                                            .clip(RoundedCornerShape(12.dp)),
//                                                        contentScale = ContentScale.Crop
//                                                    )
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

                                            }

                                        }
                                    }

                                    // Checkmark overlay
                                    if (appIcon.selected) {
                                        Image(
                                            painter = rememberAsyncImagePainter(R.drawable.ic_check),
                                            contentDescription = "Selected",
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp) // Icon size for the checkmark
                                        )
                                    }
                                }
                            }

                        }
                    }
                }

            }

            if (isDragging) {
                Box(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .fillMaxWidth() // Fill the width of the parent container
                        .height(100.dp) // Set the height of the Box
                        .align(Alignment.BottomCenter), // Align it at the bottom center of the parent
//                                   .background(if (isDraggingOverDeleteZone) Color.Red else Color.Gray)
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_delete_area),
                        contentDescription = "bg_delete_area",
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_trash),
                            contentDescription = "ic_trash",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.text_delete),
                            color = clr_FFDDDB,
                            fontFamily = AppFont.Grandstander,
                            fontSize = 14.sp
                        )
                    }
                }
            }

        }

        if (loading.value) {
            LoadingScreen()
        }
    }

}

private fun onBack(
    isChanged: Boolean,
    viewModel: ImagePickerViewModel,
    selectedMedia: MutableList<AppIcon>,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    if (isChanged) {
        // Save selected icons and navigate back only after completion
        viewModel.saveSelectedIcons(selectedMedia, onSuccess = {
            // Notify sharedViewModel that icons have changed
            sharedViewModel.setIconsChanged(true)
            // Navigate back
            navController.popBackStack()
        }, onFailure = { error ->
            // Handle error (e.g., show a toast or log)
            Log.e("onBack", "Error saving icons: ${error.message}")
        })
    } else {
        // If no changes, simply update the shared view model and navigate back
        sharedViewModel.setIconsChanged(false)
        navController.popBackStack()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ImagePickerScreen(navController = NavController(LocalContext.current))
}