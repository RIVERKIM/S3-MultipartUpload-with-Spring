package com.example.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class AWSS3Config(
    @Value("\${cloud.aws.credentials.access_key}") private val awsAccessKey: String,
    @Value("\${cloud.aws.credentials.secret_key}") private val awsSecretKey: String,
    @Value("\${cloud.aws.region.static}") private val region: String
) {
    @Bean
    fun basicAwsCredentials(): AwsCredentials =
        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)

    @Bean
    fun AWSS3Client(awsCredentials: AwsCredentials): S3Client {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build()
    }

}
