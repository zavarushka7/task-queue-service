package com.example.taskqueue.helper

import com.example.taskqueue.model.Task
import com.example.taskqueue.model.TaskStatus
import com.example.taskqueue.model.TaskType
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object TestDataFactory {

    fun createTask(
        id: UUID? = null,
        filePath: String = "/data/test.csv",
        originalFileName: String = "test.csv",
        type: TaskType = TaskType.CSV_PARSING,
        status: TaskStatus = TaskStatus.PENDING,
        priority: Int = 0,
        fileSize: Long = 1024,
        mimeType: String = "text/csv",
        retryCount: Int = 0,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
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
            createTask(
                id = UUID.randomUUID(),
                filePath = "/data/file_${index}.csv",
                originalFileName = "file_${index}.csv",
                status = status,
                priority = index % 5,
                createdAt = Instant.now().minus(index.toLong(), ChronoUnit.HOURS)
            )
        }
    }
}