package com.example.taskqueueservice.service

import com.example.taskqueueservice.dto.response.PageResponse
import com.example.taskqueueservice.dto.response.TaskEventResponse
import com.example.taskqueueservice.mapper.TaskEventMapper
import com.example.taskqueueservice.model.EventType
import com.example.taskqueueservice.model.TaskEvent
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskEventRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class TaskEventService(
    private val taskEventRepository: TaskEventRepository
) {

    fun logEvent(
        taskId: UUID,
        eventType: EventType,
        oldStatus: TaskStatus? = null,
        newStatus: TaskStatus? = null,
        details: String? = null
    ): TaskEvent {
        val event = TaskEvent(
            taskId = taskId,
            eventType = eventType,
            oldStatus = oldStatus,
            newStatus = newStatus,
            details = details
        )
        return taskEventRepository.save(event)
    }

    @Transactional(readOnly = true)
    fun getTaskEvents(taskId: UUID, page: Int, size: Int): PageResponse<TaskEventResponse> {
        val pageable = PageRequest.of(page, size)
        val events = taskEventRepository.findByTaskIdOrderByTimestampDesc(taskId, pageable)

        return PageResponse(
            content = events.content.map { TaskEventMapper.toResponse(it) },
            page = events.number,
            size = events.size,
            totalElements = events.totalElements,
            totalPages = events.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getAllTaskEvents(taskId: UUID): List<TaskEventResponse> {
        return taskEventRepository.findByTaskIdOrderByTimestampDesc(taskId)
            .map { TaskEventMapper.toResponse(it) }
    }
}