package com.example.taskqueue.model

enum class EventType {
    /**
     * Задача создана и поставлена в очередь
     */
    TASK_CREATED,

    /**
     * Статус задачи изменился (любой переход между статусами)
     */
    STATUS_CHANGED,

    /**
     * Началась обработка задачи
     */
    PROCESSING_STARTED,

    /**
     * Задача успешно завершена
     */
    TASK_COMPLETED,

    /**
     * Задача завершилась с ошибкой
     */
    TASK_FAILED,

    /**
     * Запланирована повторная попытка обработки
     */
    RETRY_SCHEDULED,

    /**
     * Задача помечена как зависшая и возвращена в очередь
     */
    TASK_STUCK,

    /**
     * Задача отменена пользователем
     */
    TASK_CANCELLED
}