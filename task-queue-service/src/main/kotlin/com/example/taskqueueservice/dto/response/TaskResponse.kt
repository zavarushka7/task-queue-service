package com.example.taskqueueservice.dto.response

import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import java.time.Instant
import java.util.UUID

data class TaskResponse(
    val id: UUID,
    val filePath: String,
    val originalFileName: String?,
    val type: TaskType,
    val status: TaskStatus,
    val priority: Int,
    val fileSize: Long?,
    val mimeType: String?,
    val result: String?,
    val errorMessage: String?,
    val retryCount: Int,
    val processingTimeMs: Long?,
    val createdAt: Instant?,
    val startedAt: Instant?,
    val completedAt: Instant?
)