package com.example.taskqueueservice.dto.request

import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import java.time.Instant

data class TaskFilterRequest(
    val status: TaskStatus? = null,
    val type: TaskType? = null,
    val priorityFrom: Int? = null,
    val priorityTo: Int? = null,
    val createdFrom: Instant? = null,
    val createdTo: Instant? = null,
    val search: String? = null
)