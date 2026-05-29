package com.example.stockmateapp.data.repository

import com.example.stockmateapp.data.remote.ApiService
import com.example.stockmateapp.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(private val api: ApiService) {

    suspend fun getDocuments(
        page: Int = 1, size: Int = 30,
        type: String? = null, status: String? = null
    ): Result<DocumentListResponse> = runCatching {
        api.getDocuments(page, size, type, status)
    }

    suspend fun getDocument(id: Int): Result<DocumentDto> = runCatching {
        api.getDocument(id)
    }

    suspend fun createDocument(req: CreateDocumentRequest): Result<DocumentDto> = runCatching {
        api.createDocument(req)
    }

    suspend fun conduct(id: Int): Result<DocumentDto> = runCatching {
        api.conductDocument(id)
    }

    suspend fun cancel(id: Int): Result<DocumentDto> = runCatching {
        api.cancelDocument(id)
    }
}
