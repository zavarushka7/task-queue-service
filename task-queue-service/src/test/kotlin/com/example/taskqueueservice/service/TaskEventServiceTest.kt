package com.example.taskqueueservice.service

import com.example.taskqueueservice.model.EventType
import com.example.taskqueueservice.model.TaskEvent
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskEventRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.UUID

class TaskEventServiceTest {

    private lateinit var taskEventRepository: TaskEventRepository
    private lateinit var taskEventService: TaskEventService

    @BeforeEach
    fun setUp() {
        taskEventRepository = mockk()
        taskEventService = TaskEventService(taskEventRepository)
    }

    @Test
    @DisplayName("Должен сохранять событие")
    fun shouldLogEvent() {
        val taskId = UUID.randomUUID()
        val event = TaskEvent(
            id = 1L,
            taskId = taskId,
            eventType = EventType.TASK_CREATED,
            newStatus = TaskStatus.PENDING,
            details = "Задача создана"
        )

        every { taskEventRepository.save(any()) } returns event

        val result = taskEventService.logEvent(
            taskId = taskId,
            eventType = EventType.TASK_CREATED,
            newStatus = TaskStatus.PENDING,
            details = "Задача создана"
        )

        assertEquals(taskId, result.taskId)
        assertEquals(EventType.TASK_CREATED, result.eventType)
        assertEquals(TaskStatus.PENDING, result.newStatus)
        verify(exactly = 1) { taskEventRepository.save(any()) }
    }

    @Test
    @DisplayName("Должен получать события задачи с пагинацией")
    fun shouldGetTaskEventsPaginated() {
        val taskId = UUID.randomUUID()
        val events = listOf(
            TaskEvent(id = 1L, taskId = taskId, eventType = EventType.TASK_CREATED, newStatus = TaskStatus.PENDING),
            TaskEvent(id = 2L, taskId = taskId, eventType = EventType.PROCESSING_STARTED, oldStatus = TaskStatus.PENDING, newStatus = TaskStatus.PROCESSING)
        )
        val page = PageImpl(events, PageRequest.of(0, 10), 2)

        every { taskEventRepository.findByTaskIdOrderByTimestampDesc(taskId, any()) } returns page

        val result = taskEventService.getTaskEvents(taskId, 0, 10)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(2, result.totalElements)
    }
}