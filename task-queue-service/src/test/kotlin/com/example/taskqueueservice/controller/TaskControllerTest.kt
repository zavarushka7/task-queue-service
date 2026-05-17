package com.example.taskqueueservice.controller

import com.example.taskqueueservice.dto.request.CreateTaskRequest
import com.example.taskqueueservice.dto.response.TaskResponse
import com.example.taskqueueservice.exception.InvalidStatusTransitionException
import com.example.taskqueueservice.exception.TaskNotFoundException
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import com.example.taskqueueservice.service.TaskService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.UUID

@WebMvcTest(TaskController::class)
@Import(TaskControllerTest.MockTaskServiceConfig::class)
class TaskControllerTest {

    @TestConfiguration
    class MockTaskServiceConfig {
        @Bean
        fun taskService(): TaskService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var taskService: TaskService

    private val objectMapper = ObjectMapper()

    @Test
    @DisplayName("POST /api/v1/tasks — должен создать задачу и вернуть 201")
    fun shouldCreateTask() {
        val request = CreateTaskRequest(
            filePath = "/data/test.csv",
            originalFileName = "test.csv",
            type = TaskType.CSV_PARSING
        )
        val response = TaskResponse(
            id = UUID.randomUUID(),
            filePath = "/data/test.csv",
            originalFileName = "test.csv",
            type = TaskType.CSV_PARSING,
            status = TaskStatus.PENDING,
            priority = 0,
            fileSize = null,
            mimeType = null,
            result = null,
            errorMessage = null,
            retryCount = 0,
            processingTimeMs = null,
            createdAt = Instant.now(),
            startedAt = null,
            completedAt = null
        )

        every { taskService.createTask(any()) } returns response

        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(response.id.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    @DisplayName("POST /api/v1/tasks — должен вернуть 400 при невалидных данных")
    fun shouldReturn400OnInvalidRequest() {
        val request = mapOf(
            "filePath" to "",
            "originalFileName" to "",
            "type" to "CSV_PARSING"
        )

        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
    @Test
    @DisplayName("GET /api/v1/tasks/{id} — должен вернуть задачу")
    fun shouldGetTask() {
        val id = UUID.randomUUID()
        val response = TaskResponse(
            id = id,
            filePath = "/data/test.csv",
            originalFileName = "test.csv",
            type = TaskType.CSV_PARSING,
            status = TaskStatus.PENDING,
            priority = 0,
            fileSize = null,
            mimeType = null,
            result = null,
            errorMessage = null,
            retryCount = 0,
            processingTimeMs = null,
            createdAt = Instant.now(),
            startedAt = null,
            completedAt = null
        )

        every { taskService.getTask(id) } returns response

        mockMvc.perform(get("/api/v1/tasks/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} — должен вернуть 404 если задача не найдена")
    fun shouldReturn404WhenTaskNotFound() {
        val id = UUID.randomUUID()
        every { taskService.getTask(id) } throws TaskNotFoundException(id)

        mockMvc.perform(get("/api/v1/tasks/$id"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET /api/v1/tasks — должен вернуть список задач")
    fun shouldGetTasks() {
        every { taskService.getTasks(any(), any(), any()) } returns mockk(relaxed = true)

        mockMvc.perform(get("/api/v1/tasks")
            .param("page", "0")
            .param("size", "10")
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{id}/cancel — должен отменить задачу")
    fun shouldCancelTask() {
        val id = UUID.randomUUID()
        val response = TaskResponse(
            id = id,
            filePath = "/data/test.csv",
            originalFileName = "test.csv",
            type = TaskType.CSV_PARSING,
            status = TaskStatus.CANCELLED,
            priority = 0,
            fileSize = null,
            mimeType = null,
            result = null,
            errorMessage = null,
            retryCount = 0,
            processingTimeMs = null,
            createdAt = Instant.now(),
            startedAt = null,
            completedAt = null
        )

        every { taskService.cancelTask(id) } returns response

        mockMvc.perform(post("/api/v1/tasks/$id/cancel"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{id}/cancel — должен вернуть 409 при неверном статусе")
    fun shouldReturn409OnInvalidCancel() {
        val id = UUID.randomUUID()
        every { taskService.cancelTask(id) } throws InvalidStatusTransitionException("Нельзя отменить")

        mockMvc.perform(post("/api/v1/tasks/$id/cancel"))
            .andExpect(status().isConflict)
    }

    @Test
    @DisplayName("POST /api/v1/tasks/{id}/process — должен запустить обработку")
    fun shouldStartProcessing() {
        val id = UUID.randomUUID()
        val response = TaskResponse(
            id = id,
            filePath = "/data/test.csv",
            originalFileName = "test.csv",
            type = TaskType.CSV_PARSING,
            status = TaskStatus.PENDING,
            priority = 0,
            fileSize = null,
            mimeType = null,
            result = null,
            errorMessage = null,
            retryCount = 0,
            processingTimeMs = null,
            createdAt = Instant.now(),
            startedAt = null,
            completedAt = null
        )

        every { taskService.startProcessing(id) } returns response

        mockMvc.perform(post("/api/v1/tasks/$id/process"))
            .andExpect(status().isAccepted)
    }
}