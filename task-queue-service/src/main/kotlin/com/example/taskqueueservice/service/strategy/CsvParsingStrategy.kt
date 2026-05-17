package com.example.taskqueueservice.service.strategy

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskType
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class CsvParsingStrategy : TaskProcessingStrategy {

    override fun supports(taskType: TaskType): Boolean = taskType == TaskType.CSV_PARSING

    override suspend fun process(task: Task): String {
        delay(2000)
        return "CSV обработан: ${task.originalFileName}, строк: 1000"
    }
}