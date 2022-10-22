package com.example.security.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean
    fun threadPoolTaskExecutor(): Executor {
        val executor: ThreadPoolTaskExecutor = ThreadPoolTaskExecutor()
        return executor.apply {
            corePoolSize = 2
            maxPoolSize = 30
            queueCapacity = 100
            setThreadNamePrefix("S3Uploader-")
            initialize()
        }
    }
}
