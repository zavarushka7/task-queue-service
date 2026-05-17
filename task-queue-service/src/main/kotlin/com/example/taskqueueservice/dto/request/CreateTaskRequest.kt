package com.example.taskqueueservice.dto.request

import com.example.taskqueueservice.model.TaskType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class CreateTaskRequest(
    @field:NotBlank(message = "Путь к файлу обязателен")
    @field:Pattern(
        regexp = "^(/[^/ ]*)+/?$",
        message = "Путь должен быть абсолютным, например: /data/file.csv"
    )
    val filePath: String,

    @field:NotBlank(message = "Имя файла обязательно")
    val originalFileName: String,

    @field:NotNull(message = "Тип задачи обязателен")
    val type: TaskType,

    val description: String? = null,

    @field:Positive(message = "Размер файла должен быть положительным")
    val fileSize: Long? = null,

    val mimeType: String? = null,

    val priority: Int = 0,

    val metadata: Map<String, Any>? = null
)