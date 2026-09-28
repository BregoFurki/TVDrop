package com.tvdrop.app

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvdrop.app.databinding.ActivityMainBinding
import com.tvdrop.app.model.TransferItem
import com.tvdrop.app.server.ServerEventListener
import com.tvdrop.app.server.TransferService
import com.tvdrop.app.ui.TransferAdapter
import com.tvdrop.app.utils.ApkParser
import com.tvdrop.app.utils.NetworkUtils
import com.tvdrop.app.utils.PackageInstallerHelper
import com.tvdrop.app.utils.QrCodeGenerator
import com.tvdrop.app.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity(), ServerEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TransferAdapter
    private val transferItems = mutableListOf<TransferItem>()

    private var transferService: TransferService? = null
    private var isBound = false
    private var pendingApkPath: String? = null
    private var latestQrBitmap: Bitmap? = null
    private var qrUrl: String? = null
    private var qrExpanded = true
    private var panelAnimator: ValueAnimator? = null
    private val connectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var networkCallbackRegistered = false
    private var initialFocusPending = true
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = requestNetworkRefresh()
        override fun onLost(network: Network) = requestNetworkRefresh()
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) =
            requestNetworkRefresh()
    }

    private val installPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val apkPath = pendingApkPath
            pendingApkPath = null
            if (apkPath != null) {
                if (PackageInstallerHelper.canInstallPackages(this)) {
                    PackageInstallerHelper.installApk(this, File(apkPath))
                } else {
                    Toast.makeText(this, "Kurulum izni verilmedi", Toast.LENGTH_LONG).show()
                }
            }
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TransferService.LocalBinder
            transferService = binder.getService()
            transferService?.listener = this@MainActivity
            isBound = true
            refreshNetworkState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            transferService = null
            isBound = false
            refreshNetworkState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingApkPath = savedInstanceState?.getString(PENDING_APK_PATH)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        setupFocusPanels()
        binding.tvServerUrl.setText(R.string.server_starting)
        binding.ivQrCode.setImageDrawable(null)
        startAndBindService()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsState()
        loadTransferredFiles()
        if (isBound) refreshNetworkState()
    }

    override fun onStart() {
        super.onStart()
        if (!networkCallbackRegistered) {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
            networkCallbackRegistered = true
        }
    }

    override fun onStop() {
        if (networkCallbackRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            networkCallbackRegistered = false
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(PENDING_APK_PATH, pendingApkPath)
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == StorageUtils.STORAGE_PERMISSION_REQUEST) {
            checkPermissionsState()
            loadTransferredFiles()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransferAdapter(
            items = transferItems,
            onInstallApk = { item ->
                requestApkInstall(item.file)
            },
            onOpenFile = { item ->
                PackageInstallerHelper.openFile(this, item.file)
            },
            onDeleteFile = { item ->
                confirmDeleteFile(item)
            },
            onActionFocused = { setQrExpanded(false) }
        )
        binding.rvTransfers.layoutManager = LinearLayoutManager(this)
        binding.rvTransfers.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnStoragePermission.setOnClickListener {
            StorageUtils.requestStoragePermission(this)
        }
        binding.btnLanguage.setText(R.string.language_title)
        binding.btnLanguage.setOnClickListener { showLanguagePicker() }
    }

    private fun showLanguagePicker() {
        val tags = arrayOf("", "en", "tr", "pt-BR", "zh-CN", "ja", "de", "id", "ru", "es", "fr")
        val names = arrayOf(
            getString(R.string.language_automatic), "English", "Türkçe", "Português (Brasil)",
            "简体中文", "日本語", "Deutsch", "Bahasa Indonesia", "Русский", "Español", "Français"
        )
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val selected = tags.indexOf(current).takeIf { it >= 0 } ?: 0
        val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert)
            .setTitle(R.string.language_title)
            .setSingleChoiceItems(names, selected) { dialog, index ->
                dialog.dismiss()
                if (index != selected) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(tags[index])
                    )
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
        dialog.show()
    }

    private fun setupFocusPanels() {
        val focusConnection = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) setQrExpanded(true)
        }
        binding.qrFrame.onFocusChangeListener = focusConnection
        binding.btnStoragePermission.onFocusChangeListener = focusConnection
        binding.rvTransfers.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) setQrExpanded(false)
        }
        binding.qrFrame.setOnClickListener { showQrDialog() }
        binding.connectionCard.isSelected = true
        binding.qrFrame.requestFocus()
    }

    private fun setQrExpanded(expanded: Boolean) {
        if (qrExpanded == expanded) return
        qrExpanded = expanded
        binding.connectionCard.isSelected = expanded
        panelAnimator?.cancel()

        val startColumnWidth = binding.connectionColumn.layoutParams.width
        val startQrSize = binding.qrFrame.layoutParams.width
        val targetColumnWidth = dp(if (expanded) 390 else 318)
        val targetQrSize = dp(if (expanded) 220 else 166)

        panelAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                binding.connectionColumn.layoutParams = binding.connectionColumn.layoutParams.apply {
                    width = (startColumnWidth + (targetColumnWidth - startColumnWidth) * fraction).toInt()
                }
                binding.qrFrame.layoutParams = binding.qrFrame.layoutParams.apply {
                    val size = (startQrSize + (targetQrSize - startQrSize) * fraction).toInt()
                    width = size
                    height = size
                }
            }
            start()
        }
    }

    private fun showQrDialog() {
        val bitmap = latestQrBitmap
        if (bitmap == null) {
            Toast.makeText(this, R.string.qr_preparing, Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        val image = ImageView(this).apply {
            setImageBitmap(bitmap)
            setBackgroundResource(R.drawable.qr_surface)
            contentDescription = getString(R.string.large_qr_code)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        val size = (minOf(resources.displayMetrics.widthPixels,
            resources.displayMetrics.heightPixels) * 0.82f).toInt()
        val container = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.bg_tv_stage)
            addView(image, FrameLayout.LayoutParams(size, size, Gravity.CENTER))
            setOnClickListener { dialog.dismiss() }
        }
        dialog.setContentView(container)
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun startAndBindService() {
        val serviceIntent = Intent(this, TransferService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun checkPermissionsState() {
        val hasPermission = StorageUtils.hasStoragePermission(this)
        binding.btnStoragePermission.visibility =
            if (hasPermission || !StorageUtils.canRequestPublicStorage()) View.GONE else View.VISIBLE
    }

    private fun requestNetworkRefresh() {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) refreshNetworkState()
        }
    }

    private fun refreshNetworkState() {
        val service = transferService
        if (service?.isServerReady != true) {
            binding.tvServerUrl.setText(R.string.server_unavailable)
            binding.tvScanHint.setText(R.string.server_unavailable_hint)
            binding.ivQrCode.setImageDrawable(null)
            latestQrBitmap = null
            qrUrl = null
            return
        }

        val ip = NetworkUtils.getLocalIpAddress()
        if (ip != null) {
            val pairedUrl = "http://$ip:8080/#token=${service.uploadToken}"
            binding.tvServerUrl.text = pairedUrl
            binding.tvScanHint.setText(R.string.scan_qr_hint)
            if (qrUrl == pairedUrl) return

            qrUrl = pairedUrl
            latestQrBitmap = null
            binding.ivQrCode.setImageDrawable(null)

            // Generate high quality QR code in background
            lifecycleScope.launch(Dispatchers.Default) {
                val qrBitmap = QrCodeGenerator.generateQrBitmap(pairedUrl, 768, 768)
                withContext(Dispatchers.Main) {
                    if (qrUrl == pairedUrl && qrBitmap != null) {
                        latestQrBitmap = qrBitmap
                        binding.ivQrCode.setImageBitmap(qrBitmap)
                    } else if (qrUrl == pairedUrl) {
                        qrUrl = null
                        binding.tvScanHint.setText(R.string.qr_failed_hint)
                    }
                }
            }
        } else {
            binding.tvServerUrl.setText(R.string.no_network)
            binding.tvScanHint.setText(R.string.no_network_hint)
            binding.ivQrCode.setImageDrawable(null)
            latestQrBitmap = null
            qrUrl = null
        }
    }

    private fun loadTransferredFiles() {
        lifecycleScope.launch(Dispatchers.IO) {
            val files = StorageUtils.getSavedFiles(this@MainActivity)
            val items = files.map { file ->
                val item = TransferItem(
                    file = file,
                    storageLabel = StorageUtils.getStorageLabel(this@MainActivity, file)
                )
                if (item.isApk) {
                    ApkParser.enrichApkDetails(this@MainActivity, item)
                }
                item
            }
            withContext(Dispatchers.Main) {
                adapter.updateItems(items)
                updateEmptyState(items.size)
                if (initialFocusPending) {
                    initialFocusPending = false
                    binding.qrFrame.post { binding.qrFrame.requestFocus() }
                }
            }
        }
    }

    private fun updateEmptyState(itemCount: Int) {
        if (itemCount == 0) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvTransfers.visibility = View.GONE
            binding.tvFileCount.text = resources.getQuantityString(R.plurals.file_count, 0, 0)
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvTransfers.visibility = View.VISIBLE
            binding.tvFileCount.text = resources.getQuantityString(R.plurals.file_count, itemCount, itemCount)
        }
    }

    private fun confirmDeleteFile(item: TransferItem) {
        val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_confirm, item.fileName, item.storageLabel))
            .setPositiveButton(R.string.btn_delete_file) { _, _ ->
                if (item.file.delete()) {
                    loadTransferredFiles()
                    Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.file_delete_failed, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
        dialog.show()
    }

    private fun requestApkInstall(file: File) {
        if (!file.isFile) {
            Toast.makeText(this, R.string.apk_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        if (PackageInstallerHelper.canInstallPackages(this)) {
            PackageInstallerHelper.installApk(this, file)
            return
        }

        pendingApkPath = file.absolutePath
        try {
            Toast.makeText(this, R.string.install_permission_prompt, Toast.LENGTH_LONG).show()
            installPermissionLauncher.launch(PackageInstallerHelper.installPermissionIntent(this))
        } catch (e: Exception) {
            pendingApkPath = null
            Toast.makeText(this, getString(R.string.permission_screen_failed, e.localizedMessage), Toast.LENGTH_LONG).show()
        }
    }

    // --- ServerEventListener Callbacks ---

    override fun onClientConnected(clientIp: String) {
        runOnUiThread {
            // Client connected
        }
    }

    override fun onFileTransferStarted(fileName: String) {
        runOnUiThread {
            binding.transferAlertCard.visibility = View.VISIBLE
            binding.tvTransferAlertTitle.text = getString(R.string.transferring_file, fileName)
            binding.transferProgressBar.isIndeterminate = true
        }
    }

    override fun onFileTransferProgress(percent: Int, bytesRead: Long, totalBytes: Long) {
        runOnUiThread {
            binding.tvTransferAlertTitle.text = getString(R.string.saving_to_tv, percent)
            binding.transferProgressBar.isIndeterminate = false
            binding.transferProgressBar.progress = percent
        }
    }

    override fun onFileTransferCompleted(file: File) {
        runOnUiThread {
            binding.transferAlertCard.visibility = View.GONE
            loadTransferredFiles()

            // If APK, prompt immediate 1-click install dialog for TV remote
            if (file.extension.equals("apk", ignoreCase = true)) {
                promptInstallApk(file)
            } else {
                Toast.makeText(this, getString(R.string.file_received, file.name), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFileTransferError(errorMessage: String) {
        runOnUiThread {
            binding.transferAlertCard.visibility = View.GONE
            Toast.makeText(this, R.string.transfer_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun promptInstallApk(file: File) {
        val tempItem = TransferItem(file = file)
        ApkParser.enrichApkDetails(this, tempItem)
        val appTitle = tempItem.appName ?: file.name

        val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert)
            .setTitle(R.string.apk_received_title)
            .setMessage(getString(R.string.apk_received_prompt, appTitle))
            .setPositiveButton(R.string.install_now) { _, _ ->
                requestApkInstall(file)
            }
            .setNegativeButton(R.string.later, null)
            .create()

        dialog.show()
        // Focus on positive button for immediate TV remote press
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        panelAnimator?.cancel()
        if (isBound) {
            transferService?.listener = null
            unbindService(serviceConnection)
            isBound = false
        }
    }

    companion object {
        private const val PENDING_APK_PATH = "pending_apk_path"
    }
}
