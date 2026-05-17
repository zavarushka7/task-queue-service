package com.example.taskqueueservice.mapper

import com.example.taskqueueservice.dto.response.TaskEventResponse
import com.example.taskqueueservice.model.TaskEvent

object TaskEventMapper {

    fun toResponse(event: TaskEvent): TaskEventResponse {
        return TaskEventResponse(
            id = event.id!!,
            taskId = event.taskId,
            eventType = event.eventType,
            oldStatus = event.oldStatus,
            newStatus = event.newStatus,
            details = event.details,
            timestamp = event.timestamp
        )
    }
}