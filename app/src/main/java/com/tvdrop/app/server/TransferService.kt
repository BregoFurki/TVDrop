package com.tvdrop.app.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.tvdrop.app.MainActivity
import com.tvdrop.app.R
import java.io.File
import java.security.SecureRandom

class TransferService : Service(), ServerEventListener {

    private val binder = LocalBinder()
    private var server: TVHttpServer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    var listener: ServerEventListener? = null
    val uploadToken: String = ByteArray(16).also(SecureRandom()::nextBytes).let {
        Base64.encodeToString(it, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
    var isServerReady: Boolean = false
        private set

    private val channelId = "tvdrop_transfer_channel"
    private val notificationId = 1001

    inner class LocalBinder : Binder() {
        fun getService(): TransferService = this@TransferService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification(getString(R.string.notification_server_active))
        startServer()
    }

    private fun startServer() {
        try {
            server = TVHttpServer(applicationContext, port = 8080, uploadToken = uploadToken, listener = this)
            server?.start()
            isServerReady = true
        } catch (e: Exception) {
            e.printStackTrace()
            server?.stop()
            server = null
            isServerReady = false
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TVDrop::TransferWakeLock")
        wakeLock?.acquire(2 * 60 * 60 * 1000L) // 2 hours max
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) wakeLock?.release()
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TVDrop Transfer Servisi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification(message: String) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("TVDrop")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
            ServiceCompat.startForeground(this, notificationId, notification, serviceType)
        } else {
            startForeground(notificationId, notification)
        }
    }

    override fun onClientConnected(clientIp: String) {
        listener?.onClientConnected(clientIp)
    }

    override fun onFileTransferStarted(fileName: String) {
        acquireWakeLock()
        updateNotification(getString(R.string.transferring_file, fileName))
        listener?.onFileTransferStarted(fileName)
    }

    override fun onFileTransferProgress(percent: Int, bytesRead: Long, totalBytes: Long) {
        listener?.onFileTransferProgress(percent, bytesRead, totalBytes)
    }

    override fun onFileTransferCompleted(file: File) {
        releaseWakeLock()
        updateNotification(getString(R.string.notification_complete, file.name))
        listener?.onFileTransferCompleted(file)
    }

    override fun onFileTransferError(errorMessage: String) {
        releaseWakeLock()
        updateNotification(getString(R.string.transfer_failed))
        listener?.onFileTransferError(errorMessage)
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("TVDrop")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        manager?.notify(notificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
        isServerReady = false
        releaseWakeLock()
    }
}
