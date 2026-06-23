package pjatk.prm.pamietnikcyfrowy

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pjatk.prm.pamietnikcyfrowy.data.AudioPlayerHelper
import pjatk.prm.pamietnikcyfrowy.data.AudioRecorderHelper
import pjatk.prm.pamietnikcyfrowy.data.CityLocation
import pjatk.prm.pamietnikcyfrowy.data.FirestoreDiaryRepository
import pjatk.prm.pamietnikcyfrowy.data.GeofenceManager
import pjatk.prm.pamietnikcyfrowy.data.LocationHelper
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry
import pjatk.prm.pamietnikcyfrowy.model.SampleData
import pjatk.prm.pamietnikcyfrowy.ui.screens.AddEntryScreen
import pjatk.prm.pamietnikcyfrowy.ui.screens.EditEntryScreen
import pjatk.prm.pamietnikcyfrowy.ui.screens.HomeScreen
import pjatk.prm.pamietnikcyfrowy.ui.screens.MapScreen
import pjatk.prm.pamietnikcyfrowy.ui.screens.PinScreen
import pjatk.prm.pamietnikcyfrowy.ui.theme.PamietnikCyfrowyTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PamietnikCyfrowyTheme {
                AppRoot()
            }
        }
    }
}

private enum class Screen {
    PIN,
    LIST,
    ADD,
    EDIT,
    MAP
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { FirestoreDiaryRepository() }
    val geofenceManager = remember { GeofenceManager(context) }

    var currentScreen by remember { mutableStateOf(Screen.PIN) }
    var pinValue by remember { mutableStateOf("") }

    val entries = remember { mutableStateListOf<DiaryEntry>() }
    var isLoaded by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newNote by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("") }
    var newLatitude by remember { mutableStateOf<Double?>(null) }
    var newLongitude by remember { mutableStateOf<Double?>(null) }
    var newPhotoUri by remember { mutableStateOf<String?>(null) }
    var newAudioUri by remember { mutableStateOf<String?>(null) }
    var isNewRecording by remember { mutableStateOf(false) }

    var selectedEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editNote by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var editLatitude by remember { mutableStateOf<Double?>(null) }
    var editLongitude by remember { mutableStateOf<Double?>(null) }
    var editPhotoUri by remember { mutableStateOf<String?>(null) }
    var editAudioUri by remember { mutableStateOf<String?>(null) }
    var isEditRecording by remember { mutableStateOf(false) }

    val locationHelper = remember { LocationHelper(context) }
    val audioRecorderHelper = remember { AudioRecorderHelper(context) }
    val audioPlayerHelper = remember { AudioPlayerHelper() }

    LaunchedEffect(Unit) {
        val loaded = repository.loadEntries()
        if (loaded.isNotEmpty()) {
            entries.clear()
            entries.addAll(loaded)
        } else {
            entries.clear()
            entries.addAll(SampleData.entries)
        }
        isLoaded = true
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            CoroutineScope(Dispatchers.Main).launch {
                val result: CityLocation? = locationHelper.getCurrentCityLocation()
                val city = result?.city ?: "Nieznane miasto"

                when (currentScreen) {
                    Screen.ADD -> {
                        newCity = city
                        newLatitude = result?.latitude
                        newLongitude = result?.longitude
                    }
                    Screen.EDIT -> {
                        editCity = city
                        editLatitude = result?.latitude
                        editLongitude = result?.longitude
                    }
                    else -> Unit
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val value = uri?.toString()
        when (currentScreen) {
            Screen.ADD -> newPhotoUri = value
            Screen.EDIT -> editPhotoUri = value
            else -> Unit
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (currentScreen == Screen.ADD) {
                if (!isNewRecording) {
                    audioRecorderHelper.startRecording()
                    isNewRecording = true
                } else {
                    newAudioUri = audioRecorderHelper.stopRecording()
                    isNewRecording = false
                }
            } else if (currentScreen == Screen.EDIT) {
                if (!isEditRecording) {
                    audioRecorderHelper.startRecording()
                    isEditRecording = true
                } else {
                    editAudioUri = audioRecorderHelper.stopRecording()
                    isEditRecording = false
                }
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    when {
        !isLoaded -> Unit

        currentScreen == Screen.PIN -> {
            PinScreen(
                pinValue = pinValue,
                onPinChange = { pinValue = it },
                onUnlockClick = {
                    if (pinValue == "1234") {
                        currentScreen = Screen.LIST
                    }
                }
            )
        }

        currentScreen == Screen.LIST -> {
            HomeScreen(
                entries = entries,
                onAddClick = { currentScreen = Screen.ADD },
                onMapClick = { currentScreen = Screen.MAP },
                onEntryClick = { entry ->
                    selectedEntry = entry
                    editTitle = entry.title
                    editNote = entry.note
                    editCity = entry.city
                    editLatitude = entry.latitude
                    editLongitude = entry.longitude
                    editPhotoUri = entry.photoUri
                    editAudioUri = entry.audioUri
                    currentScreen = Screen.EDIT
                },
                onPlayAudioClick = { path ->
                    audioPlayerHelper.play(path)
                }
            )
        }

        currentScreen == Screen.MAP -> {
            MapScreen(entries = entries)
        }

        currentScreen == Screen.ADD -> {
            AddEntryScreen(
                title = newTitle,
                note = newNote,
                city = newCity,
                photoUri = newPhotoUri,
                audioUri = newAudioUri,
                isRecording = isNewRecording,
                onTitleChange = { newTitle = it },
                onNoteChange = { newNote = it },
                onGetLocationClick = {
                    val permission = Manifest.permission.ACCESS_FINE_LOCATION
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val result = locationHelper.getCurrentCityLocation()
                            newCity = result?.city ?: "Nieznane miasto"
                            newLatitude = result?.latitude
                            newLongitude = result?.longitude
                        }
                    } else {
                        locationPermissionLauncher.launch(permission)
                    }
                },
                onPickPhotoClick = {
                    photoPickerLauncher.launch("image/*")
                },
                onToggleRecordingClick = {
                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onSaveClick = {
                    val entry = DiaryEntry(
                        id = UUID.randomUUID().toString(),
                        title = newTitle,
                        note = newNote,
                        city = if (newCity.isBlank()) "Nieznane miasto" else newCity,
                        photoUri = newPhotoUri,
                        audioUri = newAudioUri,
                        latitude = newLatitude,
                        longitude = newLongitude
                    )

                    entries.add(0, entry)

                    if (entry.latitude != null && entry.longitude != null) {
                        geofenceManager.registerGeofence(
                            requestId = entry.id,
                            latitude = entry.latitude,
                            longitude = entry.longitude
                        )
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        repository.saveEntry(entry)
                    }

                    newTitle = ""
                    newNote = ""
                    newCity = ""
                    newLatitude = null
                    newLongitude = null
                    newPhotoUri = null
                    newAudioUri = null
                    isNewRecording = false
                    currentScreen = Screen.LIST
                }
            )
        }

        currentScreen == Screen.EDIT -> {
            EditEntryScreen(
                title = editTitle,
                note = editNote,
                city = editCity,
                photoUri = editPhotoUri,
                audioUri = editAudioUri,
                isRecording = isEditRecording,
                onTitleChange = { editTitle = it },
                onNoteChange = { editNote = it },
                onCityChange = { editCity = it },
                onGetLocationClick = {
                    val permission = Manifest.permission.ACCESS_FINE_LOCATION
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val result = locationHelper.getCurrentCityLocation()
                            editCity = result?.city ?: "Nieznane miasto"
                            editLatitude = result?.latitude
                            editLongitude = result?.longitude
                        }
                    } else {
                        locationPermissionLauncher.launch(permission)
                    }
                },
                onPickPhotoClick = {
                    photoPickerLauncher.launch("image/*")
                },
                onToggleRecordingClick = {
                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onPlayAudioClick = { path ->
                    audioPlayerHelper.play(path)
                },
                onSaveClick = {
                    val index = entries.indexOfFirst { it.id == selectedEntry?.id }
                    if (index != -1 && selectedEntry != null) {
                        val updated = entries[index].copy(
                            title = editTitle,
                            note = editNote,
                            city = if (editCity.isBlank()) "Nieznane miasto" else editCity,
                            photoUri = editPhotoUri,
                            audioUri = editAudioUri,
                            latitude = editLatitude,
                            longitude = editLongitude
                        )
                        entries[index] = updated

                        CoroutineScope(Dispatchers.IO).launch {
                            repository.updateEntry(updated)
                        }
                    }
                    selectedEntry = null
                    isEditRecording = false
                    currentScreen = Screen.LIST
                }
            )
        }
    }
}