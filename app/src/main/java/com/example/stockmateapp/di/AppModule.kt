package com.example.stockmateapp.di

import android.content.Context
import com.example.stockmateapp.data.local.AppDatabase
import com.example.stockmateapp.data.local.TokenStorage
import com.example.stockmateapp.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(tokenStorage: TokenStorage): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
            defaultRequest {
                val token = tokenStorage.getAccessToken()
                if (token != null) {
                    headers.append("Authorization", "Bearer $token")
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideApiService(client: HttpClient, @ApplicationContext context: Context): ApiService {
        val url = "http://10.0.2.2:8080"
        return ApiService(client, url)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.create(context)
    }
}
