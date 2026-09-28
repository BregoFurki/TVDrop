package com.tvdrop.app.server

import android.content.Context
import com.google.gson.JsonObject
import com.tvdrop.app.utils.StorageUtils
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.Semaphore

class TVHttpServer(
    private val context: Context,
    port: Int = 8080,
    private val uploadToken: String,
    private val listener: ServerEventListener? = null
) : NanoHTTPD(port) {

    private val uploadSlot = Semaphore(1)

    companion object {
        // NanoHTTPD's multipart parser uses integer-sized buffers for file parts.
        private const val MAX_REQUEST_BYTES = 2L * 1024 * 1024 * 1024 - 1024 * 1024
    }

    init {
        // Essential on Android: Set temp file manager to app's cache directory
        // Default java.io.tmpdir is /data/local/tmp which is not writable on Android
        val cacheDir = context.cacheDir
        setTempFileManagerFactory {
            object : TempFileManager {
                private val tempFiles = mutableListOf<TempFile>()

                override fun clear() {
                    for (file in tempFiles) {
                        try {
                            file.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    tempFiles.clear()
                }

                override fun createTempFile(filename_hint: String?): TempFile {
                    val file = File.createTempFile("tvdrop_upload_", ".tmp", cacheDir)
                    val temp = object : TempFile {
                        override fun delete() {
                            file.delete()
                        }

                        override fun getName(): String = file.absolutePath

                        override fun open(): FileOutputStream = FileOutputStream(file)
                    }
                    tempFiles.add(temp)
                    return temp
                }
            }
        }
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        return try {
            when {
                uri == "/" || uri == "/index.html" -> serveWebClient()
                uri == "/i18n.js" -> serveWebTranslations()
                uri == "/api/status" -> serveStatus()
                uri == "/upload" && method == Method.POST -> {
                    if (!uploadSlot.tryAcquire()) {
                        jsonError(Response.Status.TOO_MANY_REQUESTS, "Başka bir aktarım sürüyor")
                    } else {
                        try {
                            handleFileUpload(session)
                        } finally {
                            uploadSlot.release()
                        }
                    }
                }
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener?.onFileTransferError("Aktarım başarısız oldu")
            jsonError(Response.Status.INTERNAL_ERROR, "Aktarım başarısız oldu")
        }
    }

    private fun serveWebClient(): Response {
        return try {
            val inputStream: InputStream = context.assets.open("web/index.html")
            val size = inputStream.available().toLong()
            newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", inputStream, size)
        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Web client assets not found")
        }
    }

    private fun serveWebTranslations(): Response {
        return try {
            val inputStream: InputStream = context.assets.open("web/i18n.js")
            newFixedLengthResponse(Response.Status.OK, "application/javascript; charset=UTF-8", inputStream, inputStream.available().toLong())
        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Translation assets not found")
        }
    }

    private fun serveStatus(): Response {
        val json = JsonObject().apply {
            addProperty("status", "running")
            addProperty("app", "TVDrop")
            addProperty("version", "1.1.0")
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
    }

    private fun handleFileUpload(session: IHTTPSession): Response {
        val suppliedToken = session.headers.entries
            .firstOrNull { it.key.equals("x-tvdrop-token", ignoreCase = true) }?.value.orEmpty()
        if (!MessageDigest.isEqual(
                suppliedToken.toByteArray(StandardCharsets.UTF_8),
                uploadToken.toByteArray(StandardCharsets.UTF_8)
            )) {
            return jsonError(Response.Status.UNAUTHORIZED, "Geçerli eşleştirme kodu gerekli")
        }

        val contentType = session.headers.entries
            .firstOrNull { it.key.equals("content-type", ignoreCase = true) }?.value.orEmpty()
        if (!contentType.startsWith("multipart/form-data", ignoreCase = true)) {
            return jsonError(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Dosya alanı gerekli")
        }

        val contentLength = session.headers.entries
            .firstOrNull { it.key.equals("content-length", ignoreCase = true) }
            ?.value?.toLongOrNull()
            ?: return jsonError(Response.Status.LENGTH_REQUIRED, "İstek boyutu gerekli")
        if (contentLength <= 0 || contentLength > MAX_REQUEST_BYTES) {
            return jsonError(Response.Status.PAYLOAD_TOO_LARGE, "Dosya çok büyük")
        }

        val rawParam = session.parameters["filename"]?.firstOrNull()
            ?: session.parameters["name"]?.firstOrNull()
        val rawFileName = rawParam ?: "upload_${System.currentTimeMillis()}"
        val fileName = try {
            UploadFileNames.requireSafe(rawFileName)
        } catch (_: IllegalArgumentException) {
            return jsonError(Response.Status.BAD_REQUEST, "Geçersiz dosya adı")
        }

        val saveDir = StorageUtils.getSaveDirectory(context)
        // NanoHTTPD keeps the full request and extracted part in cache before the final copy.
        if (contentLength > context.cacheDir.usableSpace / 3 ||
            contentLength > saveDir.usableSpace / 3
        ) {
            return jsonError(Response.Status.PAYLOAD_TOO_LARGE, "Yeterli boş alan yok")
        }

        listener?.onClientConnected(session.remoteIpAddress)
        listener?.onFileTransferStarted(fileName)

        val files = HashMap<String, String>()
        session.parseBody(files)

        val tempFilePath = files["file"]
        if (tempFilePath == null) {
            listener?.onFileTransferError("Dosya verisi alınamadı")
            return jsonError(Response.Status.BAD_REQUEST, "Dosya verisi alınamadı")
        }

        val tempFile = File(tempFilePath).canonicalFile
        if (tempFile.parentFile != context.cacheDir.canonicalFile ||
            !tempFile.name.startsWith("tvdrop_upload_") || !tempFile.isFile ||
            tempFile.length() > MAX_REQUEST_BYTES
        ) {
            listener?.onFileTransferError("Geçersiz yükleme verisi")
            return jsonError(Response.Status.BAD_REQUEST, "Geçersiz yükleme verisi")
        }

        var targetFile: File? = null
        try {
            targetFile = UploadFileNames.reserveUniqueFile(saveDir, fileName)
            val totalBytes = tempFile.length()
            var copiedBytes = 0L
            var lastPercent = -1
            FileInputStream(tempFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copiedBytes += count
                        val percent = if (totalBytes == 0L) 100
                            else (copiedBytes * 100 / totalBytes).toInt()
                        if (percent != lastPercent) {
                            listener?.onFileTransferProgress(percent, copiedBytes, totalBytes)
                            lastPercent = percent
                        }
                    }
                }
            }
            if (lastPercent < 100) listener?.onFileTransferProgress(100, copiedBytes, totalBytes)
            listener?.onFileTransferCompleted(targetFile)

            val json = JsonObject().apply {
                addProperty("status", "success")
                addProperty("fileName", targetFile.name)
                addProperty("fileSize", targetFile.length())
                addProperty("isApk", targetFile.extension.equals("apk", ignoreCase = true))
            }
            return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
        } catch (e: Exception) {
            targetFile?.delete()
            throw e
        } finally {
            tempFile.delete()
        }
    }

    private fun jsonError(status: Response.Status, message: String): Response {
        val json = JsonObject().apply {
            addProperty("status", "error")
            addProperty("message", message)
        }
        return newFixedLengthResponse(status, "application/json", json.toString())
    }
}
