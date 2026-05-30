package com.example.stockmateapp.di

import android.content.Context
import com.example.stockmateapp.data.local.AppDatabase
import com.example.stockmateapp.data.local.ServerUrlStorage
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(tokenStorage: TokenStorage): HttpClient {
        val trustAll = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }
        return HttpClient(CIO) {
            engine {
                https {
                    trustManager = trustAll
                }
            }
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
    fun provideApiService(client: HttpClient, serverUrlStorage: ServerUrlStorage): ApiService {
        val url = runBlocking { serverUrlStorage.serverUrl.firstOrNull() } ?: ServerUrlStorage.DEFAULT_URL
        return ApiService(client, url)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.create(context)
    }
}
