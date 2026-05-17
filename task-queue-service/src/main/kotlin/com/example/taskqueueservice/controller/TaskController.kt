package com.example.taskqueueservice.controller

import com.example.taskqueueservice.dto.request.CreateTaskRequest
import com.example.taskqueueservice.dto.request.TaskFilterRequest
import com.example.taskqueueservice.dto.response.PageResponse
import com.example.taskqueueservice.dto.response.TaskResponse
import com.example.taskqueueservice.dto.response.TaskSummaryResponse
import com.example.taskqueueservice.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Management", description = "API для управления задачами обработки файлов")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    @Operation(summary = "Создать новую задачу", description = "Создаёт задачу и ставит её в очередь на обработку")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Задача создана"),
        ApiResponse(responseCode = "400", description = "Невалидные данные"),
        ApiResponse(responseCode = "404", description = "Файл не найден")
    ])
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val task = taskService.createTask(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(task)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить задачу по ID", description = "Возвращает полную информацию о задаче")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Задача найдена"),
        ApiResponse(responseCode = "404", description = "Задача не найдена")
    ])
    fun getTask(
        @Parameter(description = "ID задачи", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.getTask(id))
    }

    @GetMapping
    @Operation(summary = "Получить список задач", description = "Возвращает список задач с фильтрацией и пагинацией")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Список задач получен")
    ])
    fun getTasks(
        @Parameter(description = "Фильтр по статусу")
        @RequestParam(required = false) status: String? = null,
        @Parameter(description = "Фильтр по типу задачи")
        @RequestParam(required = false) type: String? = null,
        @Parameter(description = "Минимальный приоритет")
        @RequestParam(required = false) priorityFrom: Int? = null,
        @Parameter(description = "Максимальный приоритет")
        @RequestParam(required = false) priorityTo: Int? = null,
        @Parameter(description = "Поиск по пути файла")
        @RequestParam(required = false) search: String? = null,
        @Parameter(description = "Номер страницы (с 0)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Размер страницы")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PageResponse<TaskSummaryResponse>> {
        val filter = TaskFilterRequest(
            status = status?.let { com.example.taskqueueservice.model.TaskStatus.valueOf(it) },
            type = type?.let { com.example.taskqueueservice.model.TaskType.valueOf(it) },
            priorityFrom = priorityFrom,
            priorityTo = priorityTo,
            search = search
        )
        return ResponseEntity.ok(taskService.getTasks(filter, page, size))
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить задачу", description = "Отменяет задачу, если она ещё не завершена")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Задача отменена"),
        ApiResponse(responseCode = "404", description = "Задача не найдена"),
        ApiResponse(responseCode = "409", description = "Нельзя отменить задачу в текущем статусе")
    ])
    fun cancelTask(
        @Parameter(description = "ID задачи")
        @PathVariable id: UUID
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.ok(taskService.cancelTask(id))
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Запустить обработку задачи", description = "Запускает фоновую обработку задачи")
    @ApiResponses(value = [
        ApiResponse(responseCode = "202", description = "Обработка запущена"),
        ApiResponse(responseCode = "404", description = "Задача не найдена"),
        ApiResponse(responseCode = "409", description = "Нельзя запустить обработку в текущем статусе")
    ])
    fun startProcessing(
        @Parameter(description = "ID задачи")
        @PathVariable id: UUID
    ): ResponseEntity<TaskResponse> {
        return ResponseEntity.accepted().body(taskService.startProcessing(id))
    }
}