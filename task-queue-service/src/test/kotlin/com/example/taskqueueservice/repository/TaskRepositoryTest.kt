package com.example.taskqueue.repository

import com.example.taskqueue.helper.TestDataFactory
import com.example.taskqueue.model.TaskStatus
import com.example.taskqueue.model.TaskType
import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.temporal.ChronoUnit

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @BeforeEach
    fun setUp() {
        taskRepository.deleteAll()
    }

    @Test
    @DisplayName("Должен сохранять задачу и находить по ID")
    fun shouldSaveAndFindTaskById() {
        // Given
        val task = TestDataFactory.createTask()

        // When
        val savedTask = taskRepository.save(task)
        val foundTask = taskRepository.findById(savedTask.id!!)

        // Then
        assertTrue(foundTask.isPresent)
        assertEquals(savedTask.id, foundTask.get().id)
        assertEquals("/data/test.csv", foundTask.get().filePath)
        assertEquals(TaskStatus.PENDING, foundTask.get().status)
    }

    @Test
    @DisplayName("Должен находить задачи по статусу")
    fun shouldFindTasksByStatus() {
        // Given
        val pendingTasks = TestDataFactory.createTaskList(3, TaskStatus.PENDING)
        val completedTasks = TestDataFactory.createTaskList(2, TaskStatus.COMPLETED)
        taskRepository.saveAll(pendingTasks + completedTasks)

        // When
        val foundPending = taskRepository.findByStatusOrderByPriorityDescCreatedAtAsc(TaskStatus.PENDING)
        val foundCompleted = taskRepository.findByStatusOrderByPriorityDescCreatedAtAsc(TaskStatus.COMPLETED)

        // Then
        assertEquals(3, foundPending.size)
        assertEquals(2, foundCompleted.size)
        foundPending.forEach { assertEquals(TaskStatus.PENDING, it.status) }
        foundCompleted.forEach { assertEquals(TaskStatus.COMPLETED, it.status) }
    }

    @Test
    @DisplayName("Должен находить следующую задачу по приоритету")
    fun shouldFindNextPendingTask() {
        // Given
        val lowPriority = TestDataFactory.createTask(
            filePath = "/data/low.csv",
            priority = 1
        )
        val highPriority = TestDataFactory.createTask(
            filePath = "/data/high.csv",
            priority = 10
        )
        val mediumPriority = TestDataFactory.createTask(
            filePath = "/data/medium.csv",
            priority = 5
        )
        taskRepository.saveAll(listOf(lowPriority, highPriority, mediumPriority))

        // When
        val nextTask = taskRepository.findNextPendingTask()

        // Then
        assertNotNull(nextTask)
        assertEquals(10, nextTask!!.priority)
        assertEquals("/data/high.csv", nextTask.filePath)
    }

    @Test
    @DisplayName("Должен находить зависшие задачи")
    fun shouldFindStuckTasks() {
        // Given
        val oldProcessingTask = TestDataFactory.createTask(
            filePath = "/data/stuck.csv",
            status = TaskStatus.PROCESSING,
            updatedAt = Instant.now().minus(20, ChronoUnit.MINUTES)
        )
        val recentProcessingTask = TestDataFactory.createTask(
            filePath = "/data/recent.csv",
            status = TaskStatus.PROCESSING,
            updatedAt = Instant.now()
        )
        taskRepository.saveAll(listOf(oldProcessingTask, recentProcessingTask))

        // When
        val timeout = Instant.now().minus(10, ChronoUnit.MINUTES)
        val stuckTasks = taskRepository.findStuckTasks(TaskStatus.PROCESSING, timeout)

        // Then
        assertEquals(1, stuckTasks.size)
        assertEquals("/data/stuck.csv", stuckTasks[0].filePath)
    }

    @Test
    @DisplayName("Должен считать задачи по статусам")
    fun shouldCountTasksByStatus() {
        // Given
        val tasks = listOf(
            TestDataFactory.createTask(status = TaskStatus.PENDING, filePath = "/data/1.csv"),
            TestDataFactory.createTask(status = TaskStatus.PENDING, filePath = "/data/2.csv"),
            TestDataFactory.createTask(status = TaskStatus.PROCESSING, filePath = "/data/3.csv"),
            TestDataFactory.createTask(status = TaskStatus.COMPLETED, filePath = "/data/4.csv")
        )
        taskRepository.saveAll(tasks)

        // When
        val counts = taskRepository.countByStatus()

        // Then
        assertTrue(counts.isNotEmpty())
        val countMap = counts.associate { it[0] as TaskStatus to (it[1] as Long).toInt() }
        assertEquals(2, countMap[TaskStatus.PENDING])
        assertEquals(1, countMap[TaskStatus.PROCESSING])
        assertEquals(1, countMap[TaskStatus.COMPLETED])
    }

    @Test
    @DisplayName("Должен находить задачи с пагинацией")
    fun shouldFindTasksWithPagination() {
        // Given
        val tasks = TestDataFactory.createTaskList(10)
        taskRepository.saveAll(tasks)

        // When
        val page = taskRepository.findAll(PageRequest.of(0, 5))

        // Then
        assertEquals(5, page.content.size)
        assertEquals(10, page.totalElements)
        assertEquals(2, page.totalPages)
    }

    @Test
    @DisplayName("Должен возвращать пустой результат для несуществующего ID")
    fun shouldReturnEmptyForNonExistentId() {
        // When
        val result = taskRepository.findById(java.util.UUID.randomUUID())

        // Then
        assertTrue(result.isEmpty)
    }
}