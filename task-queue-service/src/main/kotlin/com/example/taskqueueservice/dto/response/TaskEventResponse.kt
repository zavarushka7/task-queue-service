package com.example.taskqueueservice.dto.response

import com.example.taskqueueservice.model.EventType
import com.example.taskqueueservice.model.TaskStatus
import java.time.Instant
import java.util.UUID

data class TaskEventResponse(
    val id: Long,
    val taskId: UUID,
    val eventType: EventType,
    val oldStatus: TaskStatus?,
    val newStatus: TaskStatus?,
    val details: String?,
    val timestamp: Instant?
)