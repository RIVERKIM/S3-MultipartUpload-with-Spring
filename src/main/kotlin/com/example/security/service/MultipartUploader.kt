package com.example.security.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.time.Instant

private const val TRANSFER_EXPIRE: Long = 1200L

@Component
class MultipartUploader(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucketName: String
) {
    private val parts: MutableList<CompletedPart> = arrayListOf()
    private lateinit var uploadId: String
    private lateinit var bucketkey: String

    fun initializeUpload(key: String) {
        bucketkey = key

        val createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
            .bucket(bucketName)
            .key(bucketkey)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .expires(Instant.now().plusSeconds(TRANSFER_EXPIRE))
            .build()

        val createMultipartUploadResponse = s3Client.createMultipartUpload(createMultipartUploadRequest)

        uploadId = createMultipartUploadResponse.uploadId()
    }

    fun partUpload(bytes: ByteArray) {
        val nextPartNumber: Int = parts.size + 1

        s3Client.uploadPart(
            UploadPartRequest.builder()
                .bucket(bucketName)
                .key(bucketkey)
                .uploadId(uploadId)
                .partNumber(nextPartNumber)
                .build(), RequestBody.fromBytes(bytes)
        ).also {
            parts.add(
                CompletedPart.builder()
                    .partNumber(nextPartNumber)
                    .eTag(it.eTag())
                    .build()
            )
        }
    }

    fun completeUpload() {
        CompletedMultipartUpload.builder()
            .parts(parts)
            .build()
            .also {
                CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(bucketkey)
                    .uploadId(uploadId)
                    .multipartUpload(it)
                    .build()
                    .also {
                        s3Client.completeMultipartUpload(it)
                    }
            }
    }

    fun abort() {
        s3Client.abortMultipartUpload(
            AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(bucketkey)
                .uploadId(uploadId)
                .build()
        )
    }

    private fun isInitialized() {
        if(uploadId.isEmpty()) {
            throw IllegalStateException("")
        }
    }
}
