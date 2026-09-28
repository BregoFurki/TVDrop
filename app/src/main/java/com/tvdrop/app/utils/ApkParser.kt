package com.tvdrop.app.utils

import android.content.Context
import com.tvdrop.app.R
import com.tvdrop.app.model.TransferItem

object ApkParser {

    /**
     * Parses an uninstalled APK file to extract app name, version, and app icon.
     */
    fun enrichApkDetails(context: Context, item: TransferItem) {
        if (!item.isApk || !item.file.exists()) return

        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val packageInfo = pm.getPackageArchiveInfo(item.file.absolutePath, 0)

            if (packageInfo != null) {
                val appInfo = packageInfo.applicationInfo
                if (appInfo != null) {
                    // Crucial trick for uninstalled APKs to load assets/icons properly
                    appInfo.sourceDir = item.file.absolutePath
                    appInfo.publicSourceDir = item.file.absolutePath

                    val label = appInfo.loadLabel(pm).toString()
                    val icon = appInfo.loadIcon(pm)

                    item.appName = label.ifBlank { item.fileName }
                    item.appVersion = packageInfo.versionName ?: "v1.0"
                    item.appIcon = icon
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            item.appName = item.fileName
            item.appVersion = context.getString(R.string.unknown_version)
        }
    }
}
