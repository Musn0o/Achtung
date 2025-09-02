package com.musno.achtung

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TranslatorViewModel : ViewModel() {

    private val translator = MlKitTranslator()

    private val _state = MutableStateFlow(TranslatorState())
    val state: StateFlow<TranslatorState> = _state.asStateFlow()

    init {
        translator.downloadModelIfNotExists()
            .onEach {
                _state.value = _state.value.copy(isModelReady = true)
            }
            .catch { e ->
                _state.value = _state.value.copy(error = e.message)
            }
            .launchIn(viewModelScope)
    }

    fun translate(text: String) {
        _state.value = _state.value.copy(isTranslating = true)
        translator.translate(text)
            .onEach {
                _state.value = _state.value.copy(translatedText = it, isTranslating = false)
            }
            .catch { e ->
                _state.value = _state.value.copy(error = e.message, isTranslating = false)
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        translator.release()
    }
}

data class TranslatorState(
    val translatedText: String = "",
    val isTranslating: Boolean = false,
    val isModelReady: Boolean = false,
    val error: String? = null
)