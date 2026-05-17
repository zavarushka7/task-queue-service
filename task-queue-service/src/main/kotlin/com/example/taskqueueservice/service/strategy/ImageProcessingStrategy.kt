package com.example.taskqueueservice.service.strategy

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskType
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class ImageProcessingStrategy : TaskProcessingStrategy {

    override fun supports(taskType: TaskType): Boolean = taskType == TaskType.IMAGE_PROCESSING

    override suspend fun process(task: Task): String {
        delay(3000)
        return "Изображение обработано: ${task.originalFileName}, размер: 800x600"
    }
}