package com.example.taskqueueservice.mapper

import com.example.taskqueueservice.dto.request.CreateTaskRequest
import com.example.taskqueueservice.dto.response.TaskResponse
import com.example.taskqueueservice.dto.response.TaskSummaryResponse
import com.example.taskqueueservice.model.Task

object TaskMapper {

    fun toEntity(request: CreateTaskRequest): Task {
        return Task(
            filePath = request.filePath,
            originalFileName = request.originalFileName,
            type = request.type,
            fileSize = request.fileSize,
            mimeType = request.mimeType,
            priority = request.priority
        )
    }

    fun toResponse(task: Task): TaskResponse {
        return TaskResponse(
            id = task.id!!,
            filePath = task.filePath,
            originalFileName = task.originalFileName,
            type = task.type,
            status = task.status,
            priority = task.priority,
            fileSize = task.fileSize,
            mimeType = task.mimeType,
            result = task.result,
            errorMessage = task.errorMessage,
            retryCount = task.retryCount,
            processingTimeMs = task.processingTimeMs,
            createdAt = task.createdAt,
            startedAt = task.startedAt,
            completedAt = task.completedAt
        )
    }

    fun toSummaryResponse(task: Task): TaskSummaryResponse {
        return TaskSummaryResponse(
            id = task.id!!,
            filePath = task.filePath,
            originalFileName = task.originalFileName,
            type = task.type,
            status = task.status,
            priority = task.priority,
            createdAt = task.createdAt
        )
    }
}