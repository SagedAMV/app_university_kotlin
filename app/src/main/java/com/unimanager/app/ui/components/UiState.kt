package com.unimanager.app.ui.components

/**
 * UiState - sealed class لإدارة حالة الواجهة
 * يستخدم لعرض Loading, Success, Error بشكل منظم
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

/**
 * Extension function لتسهيل التحويل من Flow إلى UiState
 */
inline fun <T> T.toSuccess(): UiState<T> = UiState.Success(this)

/**
 * Extension function لمعالجة الأخطاء
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    return when {
        isSuccess -> UiState.Success(getOrThrow())
        isFailure -> UiState.Error(exceptionOrNull()?.message ?: "حدث خطأ غير معروف", exceptionOrNull())
        else -> UiState.Idle
    }
}
