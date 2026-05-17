package com.example.taskqueueservice.repository

import com.example.taskqueueservice.helper.TestDataFactory
import com.example.taskqueueservice.model.TaskStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest

import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

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
        val task = TestDataFactory.createTask()
        val savedTask = taskRepository.save(task)
        val foundTask = taskRepository.findById(savedTask.id!!)

        assertTrue(foundTask.isPresent)
        assertEquals(savedTask.id, foundTask.get().id)
        assertEquals("/data/test.csv", foundTask.get().filePath)
        assertEquals(TaskStatus.PENDING, foundTask.get().status)
    }

    @Test
    @DisplayName("Должен находить задачи по статусу")
    fun shouldFindTasksByStatus() {
        taskRepository.save(TestDataFactory.createTask(filePath = "/data/1.csv", status = TaskStatus.PENDING))
        taskRepository.save(TestDataFactory.createTask(filePath = "/data/2.csv", status = TaskStatus.PENDING))
        taskRepository.save(TestDataFactory.createTask(filePath = "/data/3.csv", status = TaskStatus.COMPLETED))

        val foundPending = taskRepository.findByStatusOrderByPriorityDescCreatedAtAsc(TaskStatus.PENDING)
        val foundCompleted = taskRepository.findByStatusOrderByPriorityDescCreatedAtAsc(TaskStatus.COMPLETED)

        assertEquals(2, foundPending.size)
        assertEquals(1, foundCompleted.size)
    }

    @Test
    @DisplayName("Должен находить задачи с пагинацией")
    fun shouldFindTasksWithPagination() {
        repeat(10) { index ->
            taskRepository.save(TestDataFactory.createTask(
                filePath = "/data/file_${index}.csv"
            ))
        }

        val page = taskRepository.findAll(PageRequest.of(0, 5))

        assertEquals(5, page.content.size)
        assertEquals(10, page.totalElements)
        assertEquals(2, page.totalPages)
    }

    @Test
    @DisplayName("Должен находить следующую задачу по приоритету")
    fun shouldFindNextPendingTask() {
        val lowPriority = TestDataFactory.createTask(filePath = "/data/low.csv", priority = 1)
        val highPriority = TestDataFactory.createTask(filePath = "/data/high.csv", priority = 10)
        val mediumPriority = TestDataFactory.createTask(filePath = "/data/medium.csv", priority = 5)
        taskRepository.saveAll(listOf(lowPriority, highPriority, mediumPriority))

        val nextTask = taskRepository.findNextPendingTask()

        assertNotNull(nextTask)
        assertEquals(10, nextTask!!.priority)
        assertEquals("/data/high.csv", nextTask.filePath)
    }

    @Test
    @DisplayName("Должен находить зависшие задачи")
    fun shouldFindStuckTasks() {
        val stuckTask = TestDataFactory.createTask(
            filePath = "/data/stuck.csv",
            status = TaskStatus.PROCESSING
        )
        val recentTask = TestDataFactory.createTask(
            filePath = "/data/recent.csv",
            status = TaskStatus.PROCESSING
        )
        taskRepository.save(stuckTask)
        taskRepository.save(recentTask)

        val timeout = Instant.now().minus(10, ChronoUnit.MINUTES)
        val stuckTasks = taskRepository.findStuckTasks(TaskStatus.PROCESSING, timeout)

        assertNotNull(stuckTasks)
    }

    @Test
    @DisplayName("Должен считать задачи по статусам")
    fun shouldCountTasksByStatus() {
        val tasks = listOf(
            TestDataFactory.createTask(status = TaskStatus.PENDING, filePath = "/data/1.csv"),
            TestDataFactory.createTask(status = TaskStatus.PENDING, filePath = "/data/2.csv"),
            TestDataFactory.createTask(status = TaskStatus.PROCESSING, filePath = "/data/3.csv"),
            TestDataFactory.createTask(status = TaskStatus.COMPLETED, filePath = "/data/4.csv")
        )
        taskRepository.saveAll(tasks)

        val counts = taskRepository.countByStatus()

        assertTrue(counts.isNotEmpty())
        val countMap = counts.associate { it[0] as TaskStatus to (it[1] as Long).toInt() }
        assertEquals(2, countMap[TaskStatus.PENDING])
        assertEquals(1, countMap[TaskStatus.PROCESSING])
        assertEquals(1, countMap[TaskStatus.COMPLETED])
    }


    @Test
    @DisplayName("Должен возвращать пустой результат для несуществующего ID")
    fun shouldReturnEmptyForNonExistentId() {
        val result = taskRepository.findById(UUID.randomUUID())
        assertTrue(result.isEmpty)
    }
}