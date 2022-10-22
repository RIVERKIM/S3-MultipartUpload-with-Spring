package com.example.security.service

import com.amazonaws.services.s3.AmazonS3Client
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class S3UrlService(
    private val amazonS3Client: AmazonS3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucketName: String
) {
    fun getBucketUrl(key: String) =
        amazonS3Client.getUrl(bucketName, key).toString()
}
