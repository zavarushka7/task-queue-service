package com.example.taskqueueservice.service

import com.example.taskqueueservice.config.AppConfig
import com.example.taskqueueservice.exception.FileNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class FileMetadataServiceTest {

    private lateinit var fileMetadataService: FileMetadataService
    private lateinit var appConfig: AppConfig

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        appConfig = AppConfig()
        appConfig.basePath = tempDir.toString()
        fileMetadataService = FileMetadataService(appConfig)
    }

    @Test
    @DisplayName("Должен подтверждать существование файла")
    fun shouldValidateFileExists() {
        val file = tempDir.resolve("test.csv").toFile()
        file.createNewFile()

        assertTrue(fileMetadataService.validateFileExists("/test.csv"))
    }

    @Test
    @DisplayName("Должен возвращать false для несуществующего файла")
    fun shouldReturnFalseForNonExistentFile() {
        assertFalse(fileMetadataService.validateFileExists("/nonexistent.csv"))
    }

    @Test
    @DisplayName("Должен получать метаданные существующего файла")
    fun shouldGetFileMetadata() {
        val file = tempDir.resolve("data.csv").toFile()
        file.writeText("test content")

        val metadata = fileMetadataService.getFileMetadata("/data.csv")

        assertTrue(metadata.exists)
        assertEquals(12, metadata.size)
        assertTrue(metadata.isReadable!!)
        assertNotNull(metadata.absolutePath)
    }

    @Test
    @DisplayName("Должен выбрасывать исключение для несуществующего файла")
    fun shouldThrowExceptionForNonExistentFile() {
        assertThrows(FileNotFoundException::class.java) {
            fileMetadataService.getFileMetadata("/missing.csv")
        }
    }

    @Test
    @DisplayName("Должен проверять доступность файла для чтения")
    fun shouldCheckFileAccessibility() {
        val file = tempDir.resolve("readable.csv").toFile()
        file.createNewFile()

        assertTrue(fileMetadataService.isFileAccessible("/readable.csv"))
        assertFalse(fileMetadataService.isFileAccessible("/nonexistent.csv"))
    }

    @Test
    @DisplayName("Должен корректно обрабатывать вложенные пути")
    fun shouldHandleNestedPaths() {
        val nestedDir = Files.createDirectory(tempDir.resolve("subdir"))
        val file = nestedDir.resolve("nested.csv").toFile()
        file.createNewFile()

        assertTrue(fileMetadataService.validateFileExists("/subdir/nested.csv"))
    }
}