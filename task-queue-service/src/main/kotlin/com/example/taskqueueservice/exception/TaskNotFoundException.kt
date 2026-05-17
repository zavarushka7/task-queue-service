package com.example.taskqueueservice.exception

import java.util.UUID

class TaskNotFoundException(taskId: UUID) : RuntimeException(
    "Задача с ID $taskId не найдена"
)