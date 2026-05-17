package com.example.taskqueueservice.service

import com.example.taskqueueservice.config.AppConfig
import com.example.taskqueueservice.exception.FileNotFoundException
import com.example.taskqueueservice.model.FileMetadata
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.time.Instant

@Service
class FileMetadataService(
    private val appConfig: AppConfig
) {

    fun validateFileExists(filePath: String): Boolean {
        val file = resolvePath(filePath)
        return file.exists() && file.isFile
    }

    fun getFileMetadata(filePath: String): FileMetadata {
        val file = resolvePath(filePath)

        if (!file.exists()) {
            throw FileNotFoundException(filePath)
        }

        return FileMetadata(
            exists = true,
            size = file.length(),
            mimeType = Files.probeContentType(file.toPath()),
            lastModified = Instant.ofEpochMilli(file.lastModified()),
            isReadable = file.canRead(),
            isWritable = file.canWrite(),
            absolutePath = file.absolutePath
        )
    }

    fun isFileAccessible(filePath: String): Boolean {
        val file = resolvePath(filePath)
        return file.exists() && file.canRead()
    }

    private fun resolvePath(filePath: String): File {
        val normalizedPath = filePath.trimStart('/')
        return File(appConfig.basePath, normalizedPath)
    }
}