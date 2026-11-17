package com.recompdroid

import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.security.MessageDigest
import java.util.ArrayDeque

data class AppLanguage(val index: Int, val label: String, val code: String?)

object L10n {
    val languages = listOf(
        AppLanguage(0, "Português (Brasil)", null),
        AppLanguage(1, "English (US)", null),
        AppLanguage(2, "Español", TranslateLanguage.SPANISH),
        AppLanguage(3, "Français", TranslateLanguage.FRENCH),
        AppLanguage(4, "Deutsch", TranslateLanguage.GERMAN),
        AppLanguage(5, "Italiano", TranslateLanguage.ITALIAN),
        AppLanguage(6, "日本語", TranslateLanguage.JAPANESE),
        AppLanguage(7, "한국어", TranslateLanguage.KOREAN),
        AppLanguage(8, "中文（简体）", TranslateLanguage.CHINESE),
        AppLanguage(9, "Русский", TranslateLanguage.RUSSIAN),
        AppLanguage(10, "हिन्दी", TranslateLanguage.HINDI),
        AppLanguage(11, "Bahasa Indonesia", TranslateLanguage.INDONESIAN),
    )

    enum class ModelState { OFF, DOWNLOADING, READY, WIFI_REQUIRED }

    private data class Request(val key: String, val source: String)

    private val lock = Any()
    private val cache = mutableMapOf<String, String>()
    private val queuedKeys = mutableSetOf<String>()
    private val queue = ArrayDeque<Request>()
    private val revision = mutableIntStateOf(0)
    private val modelState = mutableStateOf(ModelState.OFF)
    private var preferences: android.content.SharedPreferences? = null
    private var currentLanguage = 0
    private var currentTarget: String? = null
    private var translator: Translator? = null
    private var modelReady = false
    private var isDownloading = false
    private var translating = false

    val translationModelState: ModelState get() = modelState.value

    fun setLanguage(context: Context, languageIndex: Int) {
        val (index, _, code) = languages.getOrNull(languageIndex) ?: languages.first()
        preferences = context.applicationContext.getSharedPreferences("recomp_translation_cache", Context.MODE_PRIVATE)

        if (code == null) {
            synchronized(lock) {
                currentLanguage = index
                currentTarget = null
                translator?.close()
                translator = null
                modelReady = false
                isDownloading = false
                translating = false
                queue.clear()
                queuedKeys.clear()
                modelState.value = ModelState.OFF
                revision.intValue++
            }
            return
        }

        synchronized(lock) {
            if ((currentLanguage == index) && (modelReady || isDownloading)) return
            val previousTarget = currentTarget
            translator?.close()
            currentLanguage = index
            currentTarget = code
            modelReady = false
            isDownloading = true
            translating = false
            queue.clear()
            queuedKeys.clear()
            modelState.value = ModelState.DOWNLOADING
            val activeTranslator = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(code)
                    .build(),
            )
            translator = activeTranslator
            revision.intValue++

            activeTranslator.downloadModelIfNeeded(
                DownloadConditions.Builder().requireWifi().build(),
            ).addOnSuccessListener {
                synchronized(lock) {
                    if (translator !== activeTranslator) return@addOnSuccessListener
                    modelReady = true
                    isDownloading = false
                    modelState.value = ModelState.READY
                }
                if ((previousTarget != null) && (previousTarget != code)) {
                    val oldModel = TranslateRemoteModel.Builder(previousTarget).build()
                    RemoteModelManager.getInstance().deleteDownloadedModel(oldModel)
                }
                translateNext(activeTranslator, index)
            }.addOnFailureListener {
                synchronized(lock) {
                    if (translator !== activeTranslator) return@addOnFailureListener
                    isDownloading = false
                    modelState.value = ModelState.WIFI_REQUIRED
                }
            }
        }
    }

    fun t(pt: String, en: String, languageIndex: Int): String {
        if (languageIndex == 0) return pt
        val source = en.ifBlank { pt }
        if (languageIndex == 1) return source

        // Observe a snapshot state so every translation arrival refreshes visible Compose text.
        revision.intValue
        val key = cacheKey(languageIndex, source)
        val cached = synchronized(lock) { cache[key] } ?: preferences?.getString(key, null)
        if (cached != null) {
            synchronized(lock) { cache[key] = cached }
            return cached
        }

        synchronized(lock) {
            if ((currentLanguage == languageIndex) && queuedKeys.add(key)) {
                queue.addLast(Request(key, source))
                translator?.takeIf { modelReady && !translating }?.let {
                    translateNext(it, languageIndex)
                }
            }
        }
        return source
    }

    private fun translateNext(activeTranslator: Translator, languageIndex: Int) {
        val (key, source) = synchronized(lock) {
            if ((((translator !== activeTranslator) || !modelReady) || translating) || queue.isEmpty()) return
            translating = true
            queue.removeFirst()
        }
        activeTranslator.translate(source)
            .addOnSuccessListener { translated ->
                synchronized(lock) {
                    cache[key] = translated
                    queuedKeys.remove(key)
                    translating = false
                    preferences?.edit { putString(key, translated) }
                    revision.intValue++
                }
                translateNext(activeTranslator, languageIndex)
            }
            .addOnFailureListener {
                synchronized(lock) {
                    queuedKeys.remove(key)
                    translating = false
                }
                translateNext(activeTranslator, languageIndex)
            }
    }

    private fun cacheKey(languageIndex: Int, source: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { byte -> "%02x".format(byte) }
        return "lang_$languageIndex:$hex"
    }
}
