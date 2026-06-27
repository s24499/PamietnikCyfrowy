package pjatk.prm.pamietnikcyfrowy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pjatk.prm.pamietnikcyfrowy.data.*
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry
import pjatk.prm.pamietnikcyfrowy.ui.screens.*
import pjatk.prm.pamietnikcyfrowy.ui.theme.PamietnikCyfrowyTheme
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PamietnikCyfrowyTheme { AppRoot() } }
    }
}

private enum class Screen { PIN, LIST, ADD, EDIT, MAP, DRAW }

// Kompaktowa funkcja do kopiowania plików
private fun Context.copyUriToInternalStorage(uri: Uri): String? = try {
    contentResolver.openInputStream(uri)?.use { input ->
        val file = File(filesDir, "photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { input.copyTo(it) }
        Uri.fromFile(file).toString()
    }
} catch (e: Exception) {
    null
}

// Funkcja rozszerzająca do hashowania
private fun String.toSha256(): String {
    return MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { FirestoreDiaryRepository() }
    val geofenceManager = remember { GeofenceManager(context) }
    val locationHelper = remember { LocationHelper(context) }
    val audioRecorder = remember { AudioRecorderHelper(context) }
    val audioPlayer = remember { AudioPlayerHelper() }

    val unknownCity = stringResource(R.string.unknown_city)

    val sharedPrefs =
        remember { context.getSharedPreferences("diary_security_prefs", Context.MODE_PRIVATE) }

    // Zaktualizowany stan SharedPreferences dla PIN-u i pytania ratunkowego
    var savedHashedPin by remember { mutableStateOf(sharedPrefs.getString("hashed_pin", null)) }
    var savedQuestion by remember {
        mutableStateOf(
            sharedPrefs.getString(
                "security_question",
                null
            )
        )
    }
    var savedHashedAnswer by remember {
        mutableStateOf(
            sharedPrefs.getString(
                "hashed_answer",
                null
            )
        )
    }

    // Zaktualizowany stan formularza PIN
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var pinValue by remember { mutableStateOf("") }
    var secQuestionValue by remember { mutableStateOf("") }
    var secAnswerValue by remember { mutableStateOf("") }
    var isRecoveryMode by remember { mutableStateOf(false) }

    var currentScreen by remember { mutableStateOf(Screen.PIN) }
    var previousScreen by remember { mutableStateOf(Screen.LIST) }

    val entries = remember { mutableStateListOf<DiaryEntry>() }
    var isLoaded by remember { mutableStateOf(false) }

    // UJEDNOLICONY STAN FORMULARZA
    var selectedEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var formTitle by remember { mutableStateOf("") }
    var formNote by remember { mutableStateOf("") }
    var formCity by remember { mutableStateOf("") }
    var formLat by remember { mutableStateOf<Double?>(null) }
    var formLon by remember { mutableStateOf<Double?>(null) }
    var formPhotoUri by remember { mutableStateOf<String?>(null) }
    var formAudioUri by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var temporaryDrawingUri by remember { mutableStateOf<String?>(null) }

    fun resetForm(entry: DiaryEntry? = null) {
        selectedEntry = entry
        formTitle = entry?.title ?: ""
        formNote = entry?.note ?: ""
        formCity = entry?.city ?: ""
        formLat = entry?.latitude
        formLon = entry?.longitude
        formPhotoUri = entry?.photoUri
        formAudioUri = entry?.audioUri
        isRecording = false
    }

    LaunchedEffect(Unit) {
        entries.addAll(repository.loadEntries())
        isLoaded = true
    }

    // UJEDNOLICONE LAUNCHERY UPRAWNIEŃ
    val locationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) CoroutineScope(Dispatchers.Main).launch {
                locationHelper.getCurrentCityLocation()?.let {
                    formCity = it.city ?: unknownCity
                    formLat = it.latitude
                    formLon = it.longitude
                }
            }
        }

    val photoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                context.copyUriToInternalStorage(it)?.let { internalUri ->
                    temporaryDrawingUri = internalUri
                    previousScreen = currentScreen
                    currentScreen = Screen.DRAW
                }
            }
        }

    val audioLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                if (!isRecording) {
                    audioRecorder.startRecording()
                    isRecording = true
                } else {
                    formAudioUri = audioRecorder.stopRecording()
                    isRecording = false
                }
            }
        }

    val notificationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun saveCurrentEntry() {
        val entry = DiaryEntry(
            id = selectedEntry?.id ?: UUID.randomUUID().toString(),
            title = formTitle,
            note = formNote,
            city = formCity.ifBlank { unknownCity },
            photoUri = formPhotoUri,
            audioUri = formAudioUri,
            latitude = formLat,
            longitude = formLon
        )

        if (selectedEntry == null) entries.add(0, entry)
        else entries[entries.indexOfFirst { it.id == entry.id }] = entry

        if (entry.latitude != null && entry.longitude != null) {
            geofenceManager.registerGeofence(entry.id, entry.latitude, entry.longitude)
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (selectedEntry == null) repository.saveEntry(entry)
            else repository.updateEntry(entry)
        }
        currentScreen = Screen.LIST
    }

    if (!isLoaded) return

    when (currentScreen) {
        Screen.PIN -> {
            val mode = when {
                savedHashedPin == null -> PinMode.SETTING
                isRecoveryMode -> PinMode.RECOVERY
                else -> PinMode.UNLOCKING
            }

            // Odczytanie tekstów dla komunikatów błędu
            val errorFillAll = stringResource(R.string.pin_error_fill_all)
            val errorWrongPin = stringResource(R.string.pin_error_wrong)
            val msgResetSuccess = stringResource(R.string.pin_msg_reset_success)
            val errorWrongAnswer = stringResource(R.string.pin_error_wrong_answer)

            PinScreen(
                mode = mode,
                pinValue = pinValue,
                questionValue = secQuestionValue,
                answerValue = secAnswerValue,
                savedQuestion = savedQuestion,
                errorMessage = pinErrorMessage,
                onPinChange = { pinValue = it; pinErrorMessage = null },
                onQuestionChange = { secQuestionValue = it; pinErrorMessage = null },
                onAnswerChange = { secAnswerValue = it; pinErrorMessage = null },
                onForgotPinClick = { isRecoveryMode = true; pinErrorMessage = null; pinValue = "" },
                onBackToUnlockClick = {
                    isRecoveryMode = false; pinErrorMessage = null; secAnswerValue = ""
                },
                onActionClick = {
                    when (mode) {
                        PinMode.SETTING -> {
                            if (pinValue.length >= 4 && secQuestionValue.isNotBlank() && secAnswerValue.isNotBlank()) {
                                val hashedPin = pinValue.toSha256()
                                val hashedAns = secAnswerValue.trim().lowercase().toSha256()

                                sharedPrefs.edit {
                                    putString("hashed_pin", hashedPin)
                                    putString("security_question", secQuestionValue.trim())
                                    putString("hashed_answer", hashedAns)
                                }
                                savedHashedPin = hashedPin
                                savedQuestion = secQuestionValue.trim()
                                savedHashedAnswer = hashedAns
                                pinValue = ""; secQuestionValue = ""; secAnswerValue = ""
                                currentScreen = Screen.LIST
                            } else {
                                pinErrorMessage = errorFillAll
                            }
                        }

                        PinMode.UNLOCKING -> {
                            if (pinValue.toSha256() == savedHashedPin) {
                                pinValue = ""; currentScreen = Screen.LIST
                            } else {
                                pinErrorMessage = errorWrongPin
                            }
                        }

                        PinMode.RECOVERY -> {
                            if (secAnswerValue.trim().lowercase().toSha256() == savedHashedAnswer) {
                                sharedPrefs.edit { clear() }
                                savedHashedPin = null
                                savedQuestion = null
                                savedHashedAnswer = null
                                isRecoveryMode = false
                                secAnswerValue = ""
                                pinErrorMessage = msgResetSuccess
                            } else {
                                pinErrorMessage = errorWrongAnswer
                            }
                        }
                    }
                }
            )
        }

        Screen.LIST -> HomeScreen(
            entries = entries,
            onAddClick = { resetForm(); currentScreen = Screen.ADD },
            onMapClick = { currentScreen = Screen.MAP },
            onEntryClick = { entry -> resetForm(entry); currentScreen = Screen.EDIT },
            onPlayAudioClick = { path -> audioPlayer.play(path) }
        )

        Screen.MAP -> MapScreen(entries = entries, onBackClick = { currentScreen = Screen.LIST })

        Screen.ADD -> AddEntryScreen(
            title = formTitle, note = formNote, city = formCity,
            photoUri = formPhotoUri, audioUri = formAudioUri, isRecording = isRecording,
            onTitleChange = { formTitle = it }, onNoteChange = { formNote = it },
            onGetLocationClick = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            onPickPhotoClick = { photoLauncher.launch("image/*") },
            onToggleRecordingClick = { audioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            onSaveClick = { saveCurrentEntry() },
            onBackClick = { isRecording = false; currentScreen = Screen.LIST }
        )

        Screen.EDIT -> EditEntryScreen(
            title = formTitle,
            note = formNote,
            city = formCity,
            photoUri = formPhotoUri,
            audioUri = formAudioUri,
            isRecording = isRecording,
            onTitleChange = { formTitle = it },
            onNoteChange = { formNote = it },
            onCityChange = { formCity = it },
            onGetLocationClick = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            onPickPhotoClick = { photoLauncher.launch("image/*") },
            onToggleRecordingClick = { audioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            onPlayAudioClick = { path -> audioPlayer.play(path) },
            onSaveClick = { saveCurrentEntry() },
            onDeleteClick = {
                selectedEntry?.let { entry ->
                    entries.removeAll { it.id == entry.id }
                    CoroutineScope(Dispatchers.IO).launch { repository.deleteEntry(entry.id) }
                }
                currentScreen = Screen.LIST
            },
            onBackClick = { isRecording = false; currentScreen = Screen.LIST }
        )

        Screen.DRAW -> {
            temporaryDrawingUri?.let { uri ->
                DrawOnImageScreen(
                    photoUri = uri,
                    onSaveModified = { finalUri ->
                        formPhotoUri = finalUri; currentScreen =
                        previousScreen; temporaryDrawingUri = null
                    },
                    onCancel = {
                        formPhotoUri = temporaryDrawingUri; currentScreen =
                        previousScreen; temporaryDrawingUri = null
                    }
                )
            }
        }
    }
}