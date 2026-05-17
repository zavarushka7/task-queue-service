package com.example.taskqueueservice.service

import com.example.taskqueueservice.dto.request.CreateTaskRequest
import com.example.taskqueueservice.dto.request.TaskFilterRequest
import com.example.taskqueueservice.exception.FileNotFoundException
import com.example.taskqueueservice.exception.InvalidStatusTransitionException
import com.example.taskqueueservice.exception.TaskNotFoundException
import com.example.taskqueueservice.helper.TestDataFactory
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import com.example.taskqueueservice.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import java.util.Optional
import java.util.UUID

class TaskServiceTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var fileMetadataService: FileMetadataService
    private lateinit var taskEventService: TaskEventService
    private lateinit var taskService: TaskService

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        fileMetadataService = mockk()
        taskEventService = mockk()
        taskService = TaskService(taskRepository, fileMetadataService, taskEventService)
    }

    @Test
    @DisplayName("Должен создавать задачу")
    fun shouldCreateTask() {
        val request = CreateTaskRequest(
            filePath = "/data/test.csv",
            originalFileName = "test.csv",
            type = TaskType.CSV_PARSING
        )
        val task = TestDataFactory.createTask()

        every { fileMetadataService.validateFileExists(request.filePath) } returns true
        every { taskRepository.save(any()) } returns task
        every { taskEventService.logEvent(any(), any(), any(), any(), any()) } returns mockk()

        val result = taskService.createTask(request)

        assertEquals(task.id, result.id)
        assertEquals(TaskStatus.PENDING, result.status)
        verify(exactly = 1) { taskRepository.save(any()) }
        verify(exactly = 1) { taskEventService.logEvent(any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("Должен выбрасывать ошибку если файл не существует")
    fun shouldThrowWhenFileNotFound() {
        val request = CreateTaskRequest(
            filePath = "/data/missing.csv",
            originalFileName = "missing.csv",
            type = TaskType.CSV_PARSING
        )

        every { fileMetadataService.validateFileExists(request.filePath) } returns false

        assertThrows(FileNotFoundException::class.java) {
            taskService.createTask(request)
        }
    }

    @Test
    @DisplayName("Должен находить задачу по ID")
    fun shouldGetTaskById() {
        val task = TestDataFactory.createTask()
        every { taskRepository.findById(task.id!!) } returns Optional.of(task)

        val result = taskService.getTask(task.id!!)

        assertEquals(task.id, result.id)
        assertEquals(task.filePath, result.filePath)
    }

    @Test
    @DisplayName("Должен выбрасывать ошибку если задача не найдена")
    fun shouldThrowWhenTaskNotFound() {
        val id = UUID.randomUUID()
        every { taskRepository.findById(id) } returns Optional.empty()

        assertThrows(TaskNotFoundException::class.java) {
            taskService.getTask(id)
        }
    }


    @Test
    @DisplayName("Не должен отменять уже завершённую задачу")
    fun shouldNotCancelCompletedTask() {
        val task = TestDataFactory.createTask(status = TaskStatus.COMPLETED)
        every { taskRepository.findById(task.id!!) } returns Optional.of(task)

        assertThrows(InvalidStatusTransitionException::class.java) {
            taskService.cancelTask(task.id!!)
        }
    }

    @Test
    @DisplayName("Должен отменять задачу")
    fun shouldCancelTask() {
        val task = TestDataFactory.createTask(status = TaskStatus.PENDING)
        every { taskRepository.findById(task.id!!) } returns Optional.of(task)
        every { taskRepository.save(any()) } returns task
        every { taskEventService.logEvent(any(), any(), any(), any(), any()) } returns mockk()

        val result = taskService.cancelTask(task.id!!)

        assertEquals(TaskStatus.CANCELLED, result.status)
        verify(exactly = 1) { taskEventService.logEvent(any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("Должен запускать обработку задачи")
    fun shouldStartProcessing() {
        val task = TestDataFactory.createTask(status = TaskStatus.PENDING)
        every { taskRepository.findById(task.id!!) } returns Optional.of(task)
        every { taskRepository.save(any()) } returns task

        val result = taskService.startProcessing(task.id!!)

        assertEquals(TaskStatus.PENDING, result.status)
    }

    @Test
    @DisplayName("Не должен запускать обработку завершённой задачи")
    fun shouldNotStartProcessingCompletedTask() {
        val task = TestDataFactory.createTask(status = TaskStatus.COMPLETED)
        every { taskRepository.findById(task.id!!) } returns Optional.of(task)

        assertThrows(InvalidStatusTransitionException::class.java) {
            taskService.startProcessing(task.id!!)
        }
    }
}