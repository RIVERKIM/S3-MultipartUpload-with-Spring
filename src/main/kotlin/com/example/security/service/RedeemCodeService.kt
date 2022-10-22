package com.example.security.service

import com.example.security.service.convert.ConvertType
import com.example.security.utils.S3KeyGenerator
import org.springframework.stereotype.Service

@Service
class RedeemCodeService(
    private val documentUploadService: DocumentUploadService,
    private val s3UrlService: S3UrlService,
) {

    fun publish(convertType: ConvertType): String {
        val key = S3KeyGenerator.generateKey()
        documentUploadService.upload(key, convertType)
        return s3UrlService.getBucketUrl(key)
    }
}
