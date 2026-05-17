package com.example.taskqueueservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskQueueServiceApplication

fun main(args: Array<String>) {
    runApplication<TaskQueueServiceApplication>(*args)
}