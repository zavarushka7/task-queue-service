package com.example.taskqueue.specification

import com.example.taskqueue.helper.TestDataFactory
import com.example.taskqueue.model.TaskStatus
import com.example.taskqueue.model.TaskType
import com.example.taskqueue.repository.TaskRepository
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
class TaskSpecificationsTest {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @BeforeEach
    fun setUp() {
        taskRepository.deleteAll()
    }

    @Test
    @DisplayName("Должен фильтровать задачи по статусу")
    fun shouldFilterByStatus() {
        // Given
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(filePath = "/data/1.csv", status = TaskStatus.PENDING),
                TestDataFactory.createTask(filePath = "/data/2.csv", status = TaskStatus.COMPLETED),
                TestDataFactory.createTask(filePath = "/data/3.csv", status = TaskStatus.PENDING)
            )
        )

        // When
        val spec = TaskSpecifications.statusEquals(TaskStatus.PENDING)
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(2, result.size)
        result.forEach { assertEquals(TaskStatus.PENDING, it.status) }
    }

    @Test
    @DisplayName("Должен фильтровать задачи по типу")
    fun shouldFilterByType() {
        // Given
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(filePath = "/data/1.csv", type = TaskType.CSV_PARSING),
                TestDataFactory.createTask(filePath = "/data/2.csv", type = TaskType.IMAGE_PROCESSING),
                TestDataFactory.createTask(filePath = "/data/3.csv", type = TaskType.CSV_PARSING)
            )
        )

        // When
        val spec = TaskSpecifications.typeEquals(TaskType.CSV_PARSING)
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(2, result.size)
        result.forEach { assertEquals(TaskType.CSV_PARSING, it.type) }
    }

    @Test
    @DisplayName("Должен фильтровать задачи созданные после даты")
    fun shouldFilterByCreatedAfter() {
        // Given
        val now = Instant.now()
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(
                    filePath = "/data/old.csv",
                    createdAt = now.minus(2, ChronoUnit.HOURS)
                ),
                TestDataFactory.createTask(
                    filePath = "/data/new.csv",
                    createdAt = now
                )
            )
        )

        // When
        val spec = TaskSpecifications.createdAfter(now.minus(1, ChronoUnit.HOURS))
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(1, result.size)
        assertEquals("/data/new.csv", result[0].filePath)
    }

    @Test
    @DisplayName("Должен фильтровать задачи созданные до даты")
    fun shouldFilterByCreatedBefore() {
        // Given
        val now = Instant.now()
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(
                    filePath = "/data/old.csv",
                    createdAt = now.minus(2, ChronoUnit.HOURS)
                ),
                TestDataFactory.createTask(
                    filePath = "/data/new.csv",
                    createdAt = now
                )
            )
        )

        // When
        val spec = TaskSpecifications.createdBefore(now.minus(1, ChronoUnit.HOURS))
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(1, result.size)
        assertEquals("/data/old.csv", result[0].filePath)
    }

    @Test
    @DisplayName("Должен фильтровать задачи по пути файла")
    fun shouldFilterByFilePath() {
        // Given
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(filePath = "/data/report.csv"),
                TestDataFactory.createTask(filePath = "/data/image.png"),
                TestDataFactory.createTask(filePath = "/data/report_final.csv")
            )
        )

        // When
        val spec = TaskSpecifications.filePathContains("report")
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.filePath.contains("report")) }
    }

    @Test
    @DisplayName("Должен фильтровать задачи по приоритету")
    fun shouldFilterByPriority() {
        // Given
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(filePath = "/data/1.csv", priority = 1),
                TestDataFactory.createTask(filePath = "/data/5.csv", priority = 5),
                TestDataFactory.createTask(filePath = "/data/10.csv", priority = 10)
            )
        )

        // When
        val spec = TaskSpecifications.priorityGreaterThanOrEqual(5)
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.priority >= 5) }
    }

    @Test
    @DisplayName("Должен комбинировать фильтры через AND")
    fun shouldCombineFilters() {
        // Given
        val now = Instant.now()
        taskRepository.saveAll(
            listOf(
                TestDataFactory.createTask(
                    filePath = "/data/pending_report.csv",
                    status = TaskStatus.PENDING,
                    type = TaskType.CSV_PARSING,
                    createdAt = now
                ),
                TestDataFactory.createTask(
                    filePath = "/data/completed_report.csv",
                    status = TaskStatus.COMPLETED,
                    type = TaskType.CSV_PARSING,
                    createdAt = now
                ),
                TestDataFactory.createTask(
                    filePath = "/data/pending_image.png",
                    status = TaskStatus.PENDING,
                    type = TaskType.IMAGE_PROCESSING,
                    createdAt = now
                )
            )
        )

        // When
        val spec = TaskSpecifications.statusEquals(TaskStatus.PENDING)
            .and(TaskSpecifications.typeEquals(TaskType.CSV_PARSING))
        val result = taskRepository.findAll(spec)

        // Then
        assertEquals(1, result.size)
        assertEquals("/data/pending_report.csv", result[0].filePath)
        assertEquals(TaskStatus.PENDING, result[0].status)
        assertEquals(TaskType.CSV_PARSING, result[0].type)
    }
}