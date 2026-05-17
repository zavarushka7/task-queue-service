package com.example.taskqueueservice.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "tasks",
    indexes = [
        Index(name = "idx_tasks_status", columnList = "status"),
        Index(name = "idx_tasks_type", columnList = "type"),
        Index(name = "idx_tasks_created_at", columnList = "createdAt"),
        Index(name = "idx_tasks_queue", columnList = "status, priority DESC, createdAt ASC")
    ]
)
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    /**
     * Путь к файлу в файловой системе
     */
    @Column(name = "file_path", nullable = false, length = 1000)
    var filePath: String,

    /**
     * Оригинальное имя файла, которое загрузил пользователь
     */
    @Column(name = "original_file_name", length = 500)
    var originalFileName: String? = null,

    /**
     * Тип задачи (CSV_PARSING, IMAGE_PROCESSING, PDF_GENERATION)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var type: TaskType,

    /**
     * Текущий статус задачи
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TaskStatus = TaskStatus.PENDING,

    /**
     * Приоритет задачи (чем выше число, тем выше приоритет)
     */
    @Column(nullable = false)
    var priority: Int = 0,

    /**
     * Размер файла в байтах
     */
    @Column(name = "file_size")
    var fileSize: Long? = null,

    /**
     * MIME-тип файла (например, text/csv, image/png)
     */
    @Column(name = "mime_type", length = 200)
    var mimeType: String? = null,

    /**
     * Результат обработки (JSON строка или любой другой формат)
     */
    @Column(length = 40000)
    var result: String? = null,

    /**
     * Сообщение об ошибке, если задача завершилась неудачно
     */
    @Column(name = "error_message", length = 40000)
    var errorMessage: String? = null,

    /**
     * Количество повторных попыток обработки
     */
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    /**
     * Время обработки в миллисекундах
     */
    @Column(name = "processing_time_ms")
    var processingTimeMs: Long? = null,

    /**
     * Дата и время создания задачи
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    /**
     * Дата и время последнего обновления задачи
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,

    /**
     * Дата и время начала обработки
     */
    @Column(name = "started_at")
    var startedAt: Instant? = null,

    /**
     * Дата и время завершения обработки
     */
    @Column(name = "completed_at")
    var completedAt: Instant? = null
)