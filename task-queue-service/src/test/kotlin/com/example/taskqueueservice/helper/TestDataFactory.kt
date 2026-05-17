package com.example.taskqueueservice.helper

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object TestDataFactory {

    fun createTask(
        id: UUID? = UUID.randomUUID(),
        filePath: String = "/data/test.csv",
        originalFileName: String = "test.csv",
        type: TaskType = TaskType.CSV_PARSING,
        status: TaskStatus = TaskStatus.PENDING,
        priority: Int = 0,
        fileSize: Long = 1024,
        mimeType: String = "text/csv",
        retryCount: Int = 0,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant? = null
    ): Task {
        return Task(
            id = id,
            filePath = filePath,
            originalFileName = originalFileName,
            type = type,
            status = status,
            priority = priority,
            fileSize = fileSize,
            mimeType = mimeType,
            retryCount = retryCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun createTaskList(count: Int, status: TaskStatus = TaskStatus.PENDING): List<Task> {
        return (1..count).map { index ->
            Task(
                id = UUID.randomUUID(),
                filePath = "/data/file_${index}.csv",
                originalFileName = "file_${index}.csv",
                type = TaskType.CSV_PARSING,
                status = status,
                priority = index % 5,
                fileSize = 1024,
                mimeType = "text/csv",
                retryCount = 0,
                createdAt = Instant.now().minus(index.toLong(), ChronoUnit.HOURS)
            )
        }
    }
}