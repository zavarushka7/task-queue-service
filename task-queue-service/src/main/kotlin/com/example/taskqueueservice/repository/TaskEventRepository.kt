package com.example.taskqueueservice.repository

import com.example.taskqueueservice.model.TaskEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskEventRepository : JpaRepository<TaskEvent, Long> {

    /**
     * Найти все события задачи, отсортированные по времени (сначала новые).
     * Поддерживает пагинацию.
     */
    fun findByTaskIdOrderByTimestampDesc(taskId: UUID, pageable: Pageable): Page<TaskEvent>

    /**
     * Найти все события задачи без пагинации (для получения полной истории).
     */
    fun findByTaskIdOrderByTimestampDesc(taskId: UUID): List<TaskEvent>
}