package com.musno.achtung

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MlKitTranslator {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.GERMAN)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val germanEnglishTranslator: Translator = Translation.getClient(options)

    fun downloadModelIfNotExists(): Flow<Unit> = callbackFlow {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        germanEnglishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { 
                trySend(Unit)
                close()
            }
            .addOnFailureListener { exception ->
                close(exception)
            }
        awaitClose {  }
    }

    fun translate(text: String): Flow<String> = callbackFlow {
        germanEnglishTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                trySend(translatedText)
                close()
            }
            .addOnFailureListener { exception ->
                close(exception)
            }
        awaitClose {  }
    }

    fun release() {
        germanEnglishTranslator.close()
    }
}
