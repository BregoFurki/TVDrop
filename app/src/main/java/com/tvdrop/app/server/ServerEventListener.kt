package com.tvdrop.app.server

import java.io.File

interface ServerEventListener {
    fun onClientConnected(clientIp: String) {}
    fun onFileTransferStarted(fileName: String) {}
    fun onFileTransferProgress(percent: Int, bytesRead: Long, totalBytes: Long) {}
    fun onFileTransferCompleted(file: File) {}
    fun onFileTransferError(errorMessage: String) {}
}
