package com.tvdrop.app.model

import android.graphics.drawable.Drawable
import java.io.File

data class TransferItem(
    val id: String = System.currentTimeMillis().toString(),
    val file: File,
    val fileName: String = file.name,
    val fileSize: Long = file.length(),
    val storageLabel: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isApk: Boolean = file.extension.equals("apk", ignoreCase = true),
    var appName: String? = null,
    var appVersion: String? = null,
    var appIcon: Drawable? = null
)
