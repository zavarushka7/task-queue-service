package com.example.taskqueueservice.service

import com.example.taskqueueservice.dto.request.CreateTaskRequest
import com.example.taskqueueservice.dto.request.TaskFilterRequest
import com.example.taskqueueservice.dto.response.PageResponse
import com.example.taskqueueservice.dto.response.TaskResponse
import com.example.taskqueueservice.dto.response.TaskSummaryResponse
import com.example.taskqueueservice.exception.InvalidStatusTransitionException
import com.example.taskqueueservice.exception.TaskNotFoundException
import com.example.taskqueueservice.mapper.TaskMapper
import com.example.taskqueueservice.model.EventType
import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskRepository
import com.example.taskqueueservice.specification.TaskSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
    private val fileMetadataService: FileMetadataService,
    private val taskEventService: TaskEventService,
    private val taskQueueManager: TaskQueueManager
) {

    fun createTask(request: CreateTaskRequest): TaskResponse {
        if (!fileMetadataService.validateFileExists(request.filePath)) {
            throw com.example.taskqueueservice.exception.FileNotFoundException(request.filePath)
        }

        val task = TaskMapper.toEntity(request)
        val savedTask = taskRepository.save(task)

        taskEventService.logEvent(
            taskId = savedTask.id!!,
            eventType = EventType.TASK_CREATED,
            newStatus = TaskStatus.PENDING,
            details = "Задача создана для файла: ${request.originalFileName}"
        )
        taskQueueManager.submitTask(savedTask)
        return TaskMapper.toResponse(savedTask)
    }

    @Transactional(readOnly = true)
    fun getTask(id: UUID): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { TaskNotFoundException(id) }
        return TaskMapper.toResponse(task)
    }

    @Transactional(readOnly = true)
    fun getTasks(filter: TaskFilterRequest, page: Int, size: Int): PageResponse<TaskSummaryResponse> {
        val spec = buildSpecification(filter)
        val pageable = PageRequest.of(page, size)
        val result = taskRepository.findAll(spec, pageable)

        return PageResponse(
            content = result.content.map { TaskMapper.toSummaryResponse(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }

    fun cancelTask(id: UUID): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { TaskNotFoundException(id) }

        if (!task.status.canBeCancelled()) {
            throw InvalidStatusTransitionException(
                "Нельзя отменить задачу в статусе ${task.status}"
            )
        }

        val oldStatus = task.status
        task.status = TaskStatus.CANCELLED
        taskRepository.save(task)

        taskEventService.logEvent(
            taskId = task.id!!,
            eventType = EventType.TASK_CANCELLED,
            oldStatus = oldStatus,
            newStatus = TaskStatus.CANCELLED,
            details = "Задача отменена пользователем"
        )

        return TaskMapper.toResponse(task)
    }

    fun startProcessing(id: UUID): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { TaskNotFoundException(id) }

        if (task.status != TaskStatus.PENDING && task.status != TaskStatus.FAILED) {
            throw InvalidStatusTransitionException(
                "Нельзя запустить обработку задачи в статусе ${task.status}"
            )
        }

        if (task.status == TaskStatus.FAILED) {
            task.retryCount = 0
            task.errorMessage = null
        }

        task.status = TaskStatus.PENDING
        taskRepository.save(task)

        return TaskMapper.toResponse(task)
    }

    private fun buildSpecification(filter: TaskFilterRequest): Specification<Task> {
        val specs = mutableListOf<Specification<Task>>()

        filter.status?.let { specs.add(TaskSpecifications.statusEquals(it)) }
        filter.type?.let { specs.add(TaskSpecifications.typeEquals(it)) }
        filter.createdFrom?.let { specs.add(TaskSpecifications.createdAfter(it)) }
        filter.createdTo?.let { specs.add(TaskSpecifications.createdBefore(it)) }
        filter.search?.let { specs.add(TaskSpecifications.filePathContains(it)) }
        filter.priorityFrom?.let { specs.add(TaskSpecifications.priorityGreaterThanOrEqual(it)) }
        filter.priorityTo?.let { specs.add(TaskSpecifications.priorityLessThanOrEqual(it)) }

        return TaskSpecifications.andAll(specs)
    }
}