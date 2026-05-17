package com.example.taskqueueservice.service.strategy

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskType

interface TaskProcessingStrategy {
    fun supports(taskType: TaskType): Boolean
    suspend fun process(task: Task): String
}