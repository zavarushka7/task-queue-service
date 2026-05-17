package com.example.taskqueueservice.model

enum class TaskType {
    /**
     * Парсинг CSV файлов (извлечение данных, валидация, преобразование)
     */
    CSV_PARSING,

    /**
     * Обработка изображений (изменение размера, сжатие, конвертация форматов)
     */
    IMAGE_PROCESSING,

    /**
     * Генерация PDF документов из различных источников данных
     */
    PDF_GENERATION
}