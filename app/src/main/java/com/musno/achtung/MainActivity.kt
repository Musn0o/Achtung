package com.musno.achtung

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musno.achtung.ui.theme.AchtungTheme
import java.util.*
import androidx.compose.material3.ExposedDropdownMenuDefaults

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsInitialized = mutableStateOf(false)
    private var voices = mutableStateListOf<Voice>()
    private var errorMessage = mutableStateOf<String?>(null)
    private val viewModel: TranslatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            AchtungTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    TranslatorScreen(
                        state = state,
                        onTranslate = viewModel::translate,
                        onSpeak = { text, speed, voice ->
                            if (ttsInitialized.value) {
                                tts.setSpeechRate(speed)
                                tts.voice = voice
                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
                        voices = voices,
                        errorMessage = errorMessage.value
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.GERMAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                errorMessage.value = "German language is not available on this device."
            } else {
                val germanVoices = tts.voices.filter { it.locale.language == Locale.GERMAN.language }
                if (germanVoices.isEmpty()) {
                    errorMessage.value = "No German voices found on this device."
                } else {
                    voices.addAll(germanVoices)
                    ttsInitialized.value = true
                }
            }
        } else {
            errorMessage.value = "Failed to initialize Text-to-Speech engine."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    state: TranslatorState,
    onTranslate: (String) -> Unit,
    onSpeak: (String, Float, Voice?) -> Unit,
    voices: List<Voice>,
    errorMessage: String?
) {
    var text by remember { mutableStateOf("") }
    var speechRate by remember { mutableStateOf(1.0f) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(voices.size) {
        Log.d("TranslatorScreen", "LaunchedEffect triggered. voices.size: ${voices.size}")
        if (voices.isNotEmpty() && selectedVoice == null) {
            selectedVoice = voices[0]
            Log.d("TranslatorScreen", "Default voice selected: ${selectedVoice?.name}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
        if (state.error != null) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter German text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { onTranslate(text) },
                enabled = text.isNotBlank() && !state.isTranslating && state.isModelReady
            ) {
                if (state.isTranslating || !state.isModelReady) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Translate")
                }
            }
            Button(
                onClick = { onSpeak(text, speechRate, selectedVoice) },
                enabled = selectedVoice != null && text.isNotBlank()
            ) {
                Text("Speak")
            }
        }


        if (state.translatedText.isNotBlank()) {
            OutlinedTextField(
                value = state.translatedText,
                onValueChange = { },
                label = { Text("English Translation") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Speech Rate: ${"%.2f".format(speechRate)}")
        Slider(
            value = speechRate,
            onValueChange = { speechRate = it },
            valueRange = 0.5f..2.0f,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true,
                value = selectedVoice?.name ?: "Select Voice",
                onValueChange = { },
                label = { Text("Voice") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                voices.forEach { voice ->
                    DropdownMenuItem(
                        text = { Text(voice.name) },
                        onClick = {
                            selectedVoice = voice
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
