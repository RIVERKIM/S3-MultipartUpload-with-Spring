package com.example.security.service

import com.example.security.domain.RedeemCode
import com.example.security.domain.RedeemCodeRepository
import com.example.security.service.convert.ConvertType
import com.example.security.service.convert.RedeemCodeToDocumentConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.logging.Logger

private const val CHUNK_SIZE = 100000

@Component
class DocumentUploadService(
    private val converter: RedeemCodeToDocumentConverter,
    private val uploader: MultipartUploader,
    private val redeemCodeRepository: RedeemCodeRepository
) {

    @Async
    fun upload(key: String, convertType: ConvertType) {
        uploader.initializeUpload(key)
        var pages: Page<RedeemCode> = redeemCodeRepository.findAll(PageRequest.of(0, CHUNK_SIZE))
        while(!pages.isEmpty) {
            val converted = converter.convert(pages.content, convertType)
            uploader.partUpload(converted)
            if(pages.isLast) break;
            pages = redeemCodeRepository.findAll(pages.nextPageable())
        }
        uploader.completeUpload()
    }
}
