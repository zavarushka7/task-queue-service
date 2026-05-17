package com.example.taskqueueservice.exception

class FileNotFoundException(filePath: String) : RuntimeException(
    "Файл не найден по пути: $filePath"
)