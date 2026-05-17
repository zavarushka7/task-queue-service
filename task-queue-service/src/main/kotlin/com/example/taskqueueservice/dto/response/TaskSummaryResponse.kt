package com.example.taskqueueservice.dto.response

import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import java.time.Instant
import java.util.UUID

data class TaskSummaryResponse(
    val id: UUID,
    val filePath: String,
    val originalFileName: String?,
    val type: TaskType,
    val status: TaskStatus,
    val priority: Int,
    val createdAt: Instant?
)