package com.example.taskqueueservice.repository

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface TaskRepository : JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    /**
     * Найти следующую задачу для обработки.
     * Выбирает задачу со статусом PENDING с наивысшим приоритетом
     * и самой ранней датой создания.
     */
    @Query("""
        SELECT t FROM Task t 
        WHERE t.status = :status 
        ORDER BY t.priority DESC, t.createdAt ASC 
        LIMIT 1
    """)
    fun findNextPendingTask(@Param("status") status: TaskStatus = TaskStatus.PENDING): Task?

    /**
     * Найти все задачи с указанным статусом.
     */
    fun findByStatusOrderByPriorityDescCreatedAtAsc(status: TaskStatus): List<Task>

    /**
     * Найти зависшие задачи — задачи в статусе PROCESSING,
     * которые не обновлялись дольше указанного времени.
     */
    @Query("""
        SELECT t FROM Task t 
        WHERE t.status = :status 
        AND t.updatedAt < :updatedBefore
    """)
    fun findStuckTasks(
        @Param("status") status: TaskStatus,
        @Param("updatedBefore") updatedBefore: Instant
    ): List<Task>

    /**
     * Подсчитать количество задач по статусам.
     * Возвращает список пар [статус, количество].
     */
    @Query("""
        SELECT t.status, COUNT(t) 
        FROM Task t 
        GROUP BY t.status
    """)
    fun countByStatus(): List<Array<Any>>

    /**
     * Найти задачи с фильтрацией и пагинацией через Specification.
     * Метод унаследован от JpaSpecificationExecutor:
     * findAll(specification, pageable)
     */
}