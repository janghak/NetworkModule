package com.seattleproject.network.result

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException

/**
 * 성공 시 [action]을 실행하고, 결과를 그대로 반환 (체이닝용)
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

/**
 * 실패 시 [action]을 실행하고, 결과를 그대로 반환 (체이닝용)
 */
inline fun <T> NetworkResult<T>.onFailure(action: (NetworkResult.Failure) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Failure) action(this)
    return this
}

/**
 * 성공 데이터를 [transform]으로 변환. 실패 시 그대로 전달.
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Failure -> this
    }
}

/**
 * 성공 데이터를 꺼내거나, 실패 시 null 반환
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.Failure -> null
    }
}

/**
 * Failure를 사용자에게 보여줄 메시지로 변환
 */
fun NetworkResult.Failure.toMessage(): String {
    return when (this) {
        is NetworkResult.Failure.HttpError -> "Server error ($code): $message"
        is NetworkResult.Failure.NetworkError -> "Network error: ${throwable.message ?: "Connection failed"}"
        is NetworkResult.Failure.Unknown -> "Unexpected error: ${throwable.message ?: "Unknown"}"
    }
}

/**
 * suspend 블록을 안전하게 실행하여 NetworkResult로 래핑.
 * Ktor 예외를 적절한 Failure 타입으로 매핑한다.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: ClientRequestException) {
        // 4xx
        NetworkResult.Failure.HttpError(
            code = e.response.status.value,
            message = e.message,
        )
    } catch (e: ServerResponseException) {
        // 5xx
        NetworkResult.Failure.HttpError(
            code = e.response.status.value,
            message = e.message,
        )
    } catch (e: IOException) {
        // 네트워크 연결 실패
        NetworkResult.Failure.NetworkError(e)
    } catch (e: Exception) {
        // 기타 (파싱 에러 등)
        NetworkResult.Failure.Unknown(e)
    }
}
