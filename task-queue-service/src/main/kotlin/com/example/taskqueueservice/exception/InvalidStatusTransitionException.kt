package com.example.taskqueueservice.exception

import com.example.taskqueueservice.model.TaskStatus

class InvalidStatusTransitionException : RuntimeException {

    constructor(currentStatus: TaskStatus, targetStatus: TaskStatus) : super(
        "Невозможно перевести задачу из статуса $currentStatus в статус $targetStatus"
    )

    constructor(message: String) : super(message)
}