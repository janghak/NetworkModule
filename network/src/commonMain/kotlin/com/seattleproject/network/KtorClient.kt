package com.seattleproject.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val KtorClient = HttpClient {

    // JSON 직렬화
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // 타임아웃
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }

    // 재시도: 5xx 응답 또는 타임아웃 시 최대 3회, 지수 백오프
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
        retryOnException(maxRetries = 3, retryOnTimeout = true)
        exponentialDelay()
    }

    // 로깅: 요청/응답 헤더 출력
    install(Logging) {
        level = LogLevel.HEADERS
    }
}
