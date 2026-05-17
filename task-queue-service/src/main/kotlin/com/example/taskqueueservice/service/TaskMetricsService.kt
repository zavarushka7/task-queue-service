package com.example.taskqueueservice.service

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Service
class TaskMetricsService(
    private val meterRegistry: MeterRegistry
) {

    private val activeWorkersGauge = meterRegistry.gauge(
        "tasks.workers.active",
        AtomicInteger(0)
    )!!

    fun incrementCreated() {
        meterRegistry.counter("tasks.created.count").increment()
    }

    fun incrementCompleted() {
        meterRegistry.counter("tasks.completed.count").increment()
    }

    fun incrementFailed() {
        meterRegistry.counter("tasks.failed.count").increment()
    }

    fun incrementCancelled() {
        meterRegistry.counter("tasks.cancelled.count").increment()
    }

    fun recordProcessingTime(milliseconds: Long) {
        meterRegistry.timer("tasks.processing.time").record(milliseconds, TimeUnit.MILLISECONDS)
    }

    fun setActiveWorkers(count: Int) {
        (activeWorkersGauge as AtomicInteger).set(count)
    }
}