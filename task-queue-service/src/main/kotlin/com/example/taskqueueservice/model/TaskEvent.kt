package com.example.taskqueueservice.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "task_events",
    indexes = [
        Index(name = "idx_task_events_task_id", columnList = "taskId"),
        Index(name = "idx_task_events_task_timestamp", columnList = "taskId, timestamp DESC")
    ]
)
data class TaskEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * ID задачи, к которой относится событие
     */
    @Column(name = "task_id", nullable = false)
    val taskId: UUID,

    /**
     * Тип события (TASK_CREATED, STATUS_CHANGED, PROCESSING_STARTED и т.д.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    val eventType: EventType,

    /**
     * Предыдущий статус задачи (может быть null для первого события)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    val oldStatus: TaskStatus? = null,

    /**
     * Новый статус задачи (может быть null для событий без смены статуса)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    val newStatus: TaskStatus? = null,

    /**
     * Детальное описание события в свободной форме
     */
    @Column(length = 4000)
    val details: String? = null,

    /**
     * Дата и время события
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val timestamp: Instant? = null
)