package com.seattleproject.network.result

/**
 * 네트워크 요청 결과를 타입 안전하게 래핑하는 sealed interface.
 *
 * 사용 예시:
 * ```
 * repository.getData()
 *     .onSuccess { data -> updateUi(data) }
 *     .onFailure { failure -> showError(failure.toMessage()) }
 * ```
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>

    sealed interface Failure : NetworkResult<Nothing> {
        /** HTTP 4xx, 5xx 응답 */
        data class HttpError(val code: Int, val message: String) : Failure

        /** 연결 실패, 타임아웃 등 */
        data class NetworkError(val throwable: Throwable) : Failure

        /** 예상하지 못한 예외 (파싱 에러 등) */
        data class Unknown(val throwable: Throwable) : Failure
    }
}
