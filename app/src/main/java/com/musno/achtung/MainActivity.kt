package com.musno.achtung

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musno.achtung.ui.theme.AchtungTheme
import java.util.*

/**
 * The main activity of the application.
 * This activity hosts the main screen and manages the Text-to-Speech (TTS) engine.
 */
class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    // The Text-to-Speech engine instance.
    private lateinit var tts: TextToSpeech

    // A state to track if the TTS engine is initialized successfully.
    private var ttsInitialized = mutableStateOf(false)

    // A mutable list to hold the available German voices.
    private var voices = mutableStateListOf<Voice>()

    // A state to hold any error message related to the TTS engine.
    private var errorMessage = mutableStateOf<String?>(null)

    /**
     * Called when the activity is first created.
     * This is where we initialize the TTS engine and set up the UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the TextToSpeech engine. The onInit callback will be called when initialization is complete.
        tts = TextToSpeech(this, this)

        setContent {
            AchtungTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // The main screen of the app.
                    TtsScreen(
                        onSpeak = { text, speed, voice ->
                            // This lambda is called when the "Speak" button is clicked.
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

    /**
     * Called when the Text-to-Speech engine has been initialized.
     * @param status The initialization status.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language to German.
            val result = tts.setLanguage(Locale.GERMAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                errorMessage.value = "German language is not available on this device."
            } else {
                // Filter the available voices to get only the German ones.
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

    /**
     * Called when the activity is being destroyed.
     * This is where we shut down the TTS engine to release resources.
     */
    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}

/**
 * The main screen of the application.
 * This composable function defines the UI of the app, including the text input,
 * speech rate slider, voice selection dropdown, and the "Speak" button.
 *
 * @param onSpeak A lambda function that is called when the "Speak" button is clicked.
 * @param voices A list of available German voices.
 * @param errorMessage An optional error message to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsScreen(
    onSpeak: (String, Float, Voice?) -> Unit,
    voices: List<Voice>,
    errorMessage: String?
) {
    // State for the text input field.
    var text by remember { mutableStateOf("") }
    // State for the speech rate slider.
    var speechRate by remember { mutableStateOf(1.0f) }
    // State for the selected voice.
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    // State for the voice selection dropdown menu (expanded or not).
    var expanded by remember { mutableStateOf(false) }

    // A side effect that runs when the `voices` list changes.
    // It sets the default voice to the first one in the list.
    LaunchedEffect(voices) {
        if (voices.isNotEmpty() && selectedVoice == null) {
            selectedVoice = voices[0]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display an error message if there is one.
        if (errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Text input field for the user to enter German text.
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter German text") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Slider to control the speech rate.
        Text("Speech Rate: ${"%.2f".format(speechRate)}")
        Slider(
            value = speechRate,
            onValueChange = { speechRate = it },
            valueRange = 0.5f..2.0f
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown menu to select the voice.
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedVoice?.name ?: "Select Voice",
                onValueChange = {},
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

        Spacer(modifier = Modifier.height(16.dp))

        // Button to trigger the text-to-speech.
        // It's enabled only when a voice is selected and the text is not blank.
        Button(
            onClick = { onSpeak(text, speechRate, selectedVoice) },
            enabled = selectedVoice != null && text.isNotBlank()
        ) {
            Text("Speak")
        }
    }
}