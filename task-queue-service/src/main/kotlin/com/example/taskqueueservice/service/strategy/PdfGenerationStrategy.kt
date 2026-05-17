package com.example.taskqueueservice.service.strategy

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskType
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class PdfGenerationStrategy : TaskProcessingStrategy {

    override fun supports(taskType: TaskType): Boolean = taskType == TaskType.PDF_GENERATION

    override suspend fun process(task: Task): String {
        delay(5000)
        return "PDF сгенерирован: ${task.originalFileName}, страниц: 10"
    }
}