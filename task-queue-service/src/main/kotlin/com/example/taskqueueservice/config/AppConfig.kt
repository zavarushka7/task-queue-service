package com.example.taskqueueservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.storage")
class AppConfig {
    var basePath: String = "/data/uploads"
}