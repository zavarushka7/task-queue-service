package com.example.taskqueue.model

import java.time.Instant

/**
 * Метаданные файла, полученные из файловой системы.
 */
data class FileMetadata(
    /**
     * Существует ли файл
     */
    val exists: Boolean,

    /**
     * Размер файла в байтах (null если файл не существует)
     */
    val size: Long? = null,

    /**
     * MIME-тип файла, определённый операционной системой (null если не удалось определить)
     */
    val mimeType: String? = null,

    /**
     * Дата и время последнего изменения файла
     */
    val lastModified: Instant? = null,

    /**
     * Доступен ли файл для чтения
     */
    val isReadable: Boolean? = null,

    /**
     * Доступен ли файл для записи
     */
    val isWritable: Boolean? = null,

    /**
     * Абсолютный путь к файлу
     */
    val absolutePath: String? = null
)