package com.example.taskqueueservice.exception

import com.example.taskqueueservice.model.TaskType

class UnsupportedTaskTypeException(taskType: TaskType) : RuntimeException(
    "Неподдерживаемый тип задачи: $taskType"
)