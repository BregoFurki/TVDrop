package com.tvdrop.app.server

import java.io.File
import java.io.IOException

internal object UploadFileNames {
    private const val MAX_NAME_LENGTH = 200

    fun requireSafe(fileName: String): String {
        require(fileName.isNotBlank() && fileName.length <= MAX_NAME_LENGTH) {
            "Geçersiz dosya adı"
        }
        require(fileName != "." && fileName != "..") { "Geçersiz dosya adı" }
        require(fileName.none { it == '/' || it == '\\' || it.isISOControl() }) {
            "Dosya adı klasör yolu içeremez"
        }
        return fileName
    }

    /** Reserves a new name atomically so simultaneous uploads cannot overwrite each other. */
    fun reserveUniqueFile(directory: File, fileName: String): File {
        val safeName = requireSafe(fileName)
        val canonicalDirectory = directory.canonicalFile
        if (!canonicalDirectory.isDirectory) throw IOException("Kayıt klasörü kullanılamıyor")

        val extensionIndex = safeName.lastIndexOf('.').takeIf { it > 0 } ?: safeName.length
        val stem = safeName.substring(0, extensionIndex)
        val extension = safeName.substring(extensionIndex)

        for (counter in 0..10_000) {
            val candidateName = if (counter == 0) safeName else "$stem ($counter)$extension"
            val candidate = File(canonicalDirectory, candidateName).canonicalFile
            if (candidate.parentFile != canonicalDirectory) {
                throw IOException("Dosya yolu kayıt klasörü dışında")
            }
            if (candidate.createNewFile()) return candidate
        }
        throw IOException("Benzersiz dosya adı oluşturulamadı")
    }
}
