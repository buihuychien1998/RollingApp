package com.example.rollingicon.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

class PermissionUtils(
    private val context: Context,
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>>
) {

    /**
     * Request permissions dynamically based on Android version.
     * @param permissions Permissions to request.
     */
    fun requestPermissions(vararg permissions: String) {
        requestPermissionLauncher.launch(arrayOf(*permissions)) // Convert to Array<String>
    }

    /**
     * Request storage permissions for the device's version.
     */
    fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(*permissions)
    }


    companion object {
        /**
         * Handle permissions granted.
         */
        var onPermissionsGranted: Context.() -> Unit = {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        }

        /**
         * Handle permissions denied.
         */
        var onPermissionsDenied: Context.(deniedPermissions: List<String>) -> Unit = {  deniedPermissions ->
            Toast.makeText(
                this,
                "Permissions denied: ${deniedPermissions.joinToString(", ")}",
                Toast.LENGTH_SHORT
            ).show()
        }

        /**
         * Handle permissions permanently denied and suggest going to settings.
         */
        var onPermissionsPermanentlyDenied: Context.(deniedPermissions: List<String>) -> Unit = { deniedPermissions ->
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(
                    "The following permissions are permanently denied: ${
                        deniedPermissions.joinToString(", ")
                    }. Please go to app settings to enable them."
                )
                .setPositiveButton("Open Settings") { _, _ ->
                    openAppSettings(this)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        /**
         * Opens the app settings for the user to manually enable permissions.
         */
        private fun openAppSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }
}