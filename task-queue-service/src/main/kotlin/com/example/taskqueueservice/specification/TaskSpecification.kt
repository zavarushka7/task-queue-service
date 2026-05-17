package com.example.taskqueueservice.specification

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.model.TaskType
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.time.Instant

object TaskSpecifications {

    /**
     * Фильтр по статусу задачи.
     */
    fun statusEquals(status: TaskStatus): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.equal(root.get<String>("status"), status.name)
        }
    }

    /**
     * Фильтр по типу задачи.
     */
    fun typeEquals(type: TaskType): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.equal(root.get<String>("type"), type.name)
        }
    }

    /**
     * Фильтр: задачи созданные после указанной даты.
     */
    fun createdAfter(date: Instant): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date)
        }
    }

    /**
     * Фильтр: задачи созданные до указанной даты.
     */
    fun createdBefore(date: Instant): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date)
        }
    }

    /**
     * Поиск по пути к файлу (содержит подстроку, без учёта регистра).
     */
    fun filePathContains(search: String): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("filePath")),
                "%" + search + "%"
            )
        }
    }

    /**
     * Фильтр по минимальному приоритету.
     */
    fun priorityGreaterThanOrEqual(minPriority: Int): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.greaterThanOrEqualTo(root.get<Int>("priority"), minPriority)
        }
    }

    /**
     * Фильтр по максимальному приоритету.
     */
    fun priorityLessThanOrEqual(maxPriority: Int): Specification<Task> {
        return Specification { root: Root<Task>, _: Any?, criteriaBuilder: CriteriaBuilder ->
            criteriaBuilder.lessThanOrEqualTo(root.get<Int>("priority"), maxPriority)
        }
    }
}