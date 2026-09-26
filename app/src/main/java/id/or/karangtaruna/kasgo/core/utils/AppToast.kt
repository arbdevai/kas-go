package id.or.karangtaruna.kasgo.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ToastType {
    SUCCESS, ERROR, INFO, WARNING
}

data class ToastMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val title: String? = null,
    val type: ToastType = ToastType.SUCCESS,
    val durationMillis: Long = 2600L
)

object AppToast {
    private val _currentToast = MutableStateFlow<ToastMessage?>(null)
    val currentToast = _currentToast.asStateFlow()

    private var dismissJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun success(message: String, title: String? = null) {
        show(message, title, ToastType.SUCCESS)
    }

    fun error(message: String, title: String? = null) {
        show(message, title, ToastType.ERROR)
    }

    fun info(message: String, title: String? = null) {
        show(message, title, ToastType.INFO)
    }

    fun warning(message: String, title: String? = null) {
        show(message, title, ToastType.WARNING)
    }

    fun show(
        message: String,
        title: String? = null,
        type: ToastType = ToastType.SUCCESS,
        durationMillis: Long = 2600L
    ) {
        dismissJob?.cancel()
        val toast = ToastMessage(
            message = message,
            title = title,
            type = type,
            durationMillis = durationMillis
        )
        _currentToast.value = toast

        dismissJob = scope.launch {
            delay(durationMillis)
            if (_currentToast.value?.id == toast.id) {
                _currentToast.value = null
            }
        }
    }

    fun dismiss() {
        dismissJob?.cancel()
        _currentToast.value = null
    }
}
