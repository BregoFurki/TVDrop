package com.tvdrop.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.tvdrop.app.R
import java.io.File

object StorageUtils {
    const val STORAGE_PERMISSION_REQUEST = 1001

    /** Whether the shared Downloads/TVDrop directory can be written directly. */
    fun hasStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Environment.isExternalStorageManager()
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> false
            else -> ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun canRequestPublicStorage(): Boolean = Build.VERSION.SDK_INT != Build.VERSION_CODES.Q

    fun requestStoragePermission(activity: Activity) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    }
                    activity.startActivity(intent)
                } catch (_: Exception) {
                    activity.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST
            )
        }
    }

    /**
     * Returns the target directory where transferred files are stored.
     */
    fun getSaveDirectory(context: Context): File {
        val targetDir = if (hasStoragePermission(context)) publicDirectory()
            else privateDirectory(context)

        if (!targetDir.isDirectory && !targetDir.mkdirs()) {
            throw java.io.IOException("Kayıt klasörü oluşturulamadı")
        }
        return targetDir
    }

    /**
     * Gets a list of previously transferred files, sorted newest first.
     */
    fun getSavedFiles(context: Context): List<File> {
        val dirs = mutableListOf(privateDirectory(context))
        if (hasStoragePermission(context)) dirs.add(publicDirectory())
        return dirs.flatMap { it.listFiles()?.toList().orEmpty() }
            .filter { it.isFile }
            .distinctBy { it.absolutePath }
            .sortedByDescending { it.lastModified() }
    }

    /** Label the physical copy shown in the file list. */
    fun getStorageLabel(context: Context, file: File): String {
        return runCatching {
            val parent = file.parentFile?.canonicalFile
            when (parent) {
                publicDirectory().canonicalFile -> context.getString(R.string.storage_downloads)
                privateDirectory(context).canonicalFile -> context.getString(R.string.storage_app)
                else -> context.getString(R.string.storage_other)
            }
        }.getOrDefault(context.getString(R.string.storage_other))
    }

    private fun privateDirectory(context: Context): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, "TVDrop")

    private fun publicDirectory(): File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TVDrop"
    )
}
