package com.example.taskqueueservice.model

enum class TaskStatus {
    /**
     * Задача создана и ожидает обработки в очереди
     */
    PENDING,

    /**
     * Задача взята в обработку одним из воркеров
     */
    PROCESSING,

    /**
     * Обработка успешно завершена, результат сохранён
     */
    COMPLETED,

    /**
     * Обработка завершилась ошибкой, все попытки исчерпаны
     */
    FAILED,

    /**
     * Задача отменена пользователем до завершения обработки
     */
    CANCELLED,

    /**
     * Задача зависла (воркер упал или превышен таймаут обработки)
     */
    STUCK;

    /**
     * Проверяет, является ли статус конечным (задача больше не будет обрабатываться)
     */
    fun isFinal(): Boolean = this == COMPLETED || this == FAILED || this == CANCELLED

    /**
     * Проверяет, можно ли отменить задачу в этом статусе
     */
    fun canBeCancelled(): Boolean = this == PENDING || this == PROCESSING || this == STUCK
}