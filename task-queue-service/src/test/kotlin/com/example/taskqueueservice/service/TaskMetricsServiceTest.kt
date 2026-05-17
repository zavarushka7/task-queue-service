package com.example.taskqueueservice.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TaskMetricsServiceTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var taskMetricsService: TaskMetricsService

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        taskMetricsService = TaskMetricsService(meterRegistry)
    }

    @Test
    @DisplayName("Должен увеличивать счётчик созданных задач")
    fun shouldIncrementCreatedCounter() {
        taskMetricsService.incrementCreated()
        taskMetricsService.incrementCreated()

        val counter = meterRegistry.find("tasks.created.count").counter()
        assertNotNull(counter)
        assertEquals(2.0, counter!!.count())
    }

    @Test
    @DisplayName("Должен увеличивать счётчик завершённых задач")
    fun shouldIncrementCompletedCounter() {
        taskMetricsService.incrementCompleted()

        val counter = meterRegistry.find("tasks.completed.count").counter()
        assertNotNull(counter)
        assertEquals(1.0, counter!!.count())
    }

    @Test
    @DisplayName("Должен увеличивать счётчик проваленных задач")
    fun shouldIncrementFailedCounter() {
        taskMetricsService.incrementFailed()
        taskMetricsService.incrementFailed()
        taskMetricsService.incrementFailed()

        val counter = meterRegistry.find("tasks.failed.count").counter()
        assertNotNull(counter)
        assertEquals(3.0, counter!!.count())
    }

    @Test
    @DisplayName("Должен обновлять количество активных воркеров")
    fun shouldSetActiveWorkers() {
        taskMetricsService.setActiveWorkers(3)

        val gauge = meterRegistry.find("tasks.workers.active").gauge()
        assertNotNull(gauge)
        assertEquals(3.0, gauge!!.value())
    }
}