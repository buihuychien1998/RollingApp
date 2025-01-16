package com.example.rollingicon.utils.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rollingicon.ui.share_view_model.GlobalStateViewModel

@Composable
fun SafeClick(
    viewModel: GlobalStateViewModel = viewModel(),
    onClick: () -> Unit,
    content: @Composable (enabled: Boolean, onClick: () -> Unit) -> Unit
) {
    val enabled = viewModel.enabled.value

    // Use the content composable to render buttons, text, etc.
    content(enabled) {
        viewModel.disableTemporarily()
        if (enabled) onClick()
    }
}



@Composable
fun SafeClickable(
    viewModel: GlobalStateViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable (enabled: Boolean) -> Unit
) {
    val enabled = viewModel.enabled.value

    Box(
        modifier = modifier
            .clickable(enabled) {
                viewModel.disableTemporarily()
                if (enabled) onClick()
            }
    ) {
        content(enabled)
    }
}