package pjatk.prm.pamietnikcyfrowy.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pjatk.prm.pamietnikcyfrowy.R
import java.io.File
import java.io.FileOutputStream

enum class DrawMode { DRAW, TEXT }
data class DrawnPath(val path: Path, val color: Color, val strokeWidth: Float)
data class DrawnText(val text: String, val position: Offset, val color: Color, val textSize: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawOnImageScreen(photoUri: String, onSaveModified: (String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val undoStack = remember { mutableStateListOf<Any>() }
    val paths = remember { mutableStateListOf<DrawnPath>() }
    val texts = remember { mutableStateListOf<DrawnText>() }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var currentMode by remember { mutableStateOf(DrawMode.DRAW) }
    var activeColor by remember { mutableStateOf(Color.Red) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var showTextDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var textTapOffset by remember { mutableStateOf(Offset.Zero) }
    var currentInputText by remember { mutableStateOf("") }

    val availableColors = listOf(
        Color.Red,
        Color.Blue,
        Color.Black,
        Color.White,
        Color.Green,
        Color.Magenta,
        Color.Yellow
    )

    LaunchedEffect(photoUri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(Uri.parse(photoUri))?.use {
                    originalBitmap =
                        BitmapFactory.decodeStream(it)?.copy(Bitmap.Config.ARGB_8888, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text(stringResource(R.string.draw_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = currentInputText,
                    onValueChange = { currentInputText = it },
                    label = { Text(stringResource(R.string.draw_dialog_label)) })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (currentInputText.isNotBlank()) {
                        val t =
                            DrawnText(currentInputText, textTapOffset, activeColor, 80f); texts.add(
                            t
                        ); undoStack.add(t)
                    }
                    showTextDialog = false; currentInputText = ""
                }) { Text(stringResource(R.string.draw_dialog_add)) }
            }
        )
    }

    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false }, title = { Text("Wybierz kolor") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    availableColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .clickable { activeColor = color; showColorDialog = false })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showColorDialog = false }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.draw_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            null
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (undoStack.isNotEmpty()) {
                            val last = undoStack.removeAt(undoStack.lastIndex) // ZMIANA TUTAJ
                            if (last is DrawnPath) paths.remove(last) else texts.remove(last)
                        }
                    }) { Text(stringResource(R.string.draw_btn_undo)) }
                    TextButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            originalBitmap?.let { bmp ->
                                val newUri =
                                    saveImageWithDrawings(context, bmp, paths, texts, canvasSize)
                                withContext(Dispatchers.Main) {
                                    if (newUri != null) onSaveModified(
                                        newUri
                                    )
                                }
                            }
                        }
                    }) { Text(stringResource(R.string.draw_btn_save)) }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    currentMode = DrawMode.DRAW
                }) { Text(stringResource(R.string.draw_mode_draw)) }
                Button(onClick = {
                    currentMode = DrawMode.TEXT
                }) { Text(stringResource(R.string.draw_mode_text)) }
                Button(onClick = {
                    showColorDialog = true
                }) { Text(stringResource(R.string.draw_btn_color)) }
            }
        }
    ) { innerPadding ->
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .pointerInput(currentMode) {
                    if (currentMode == DrawMode.DRAW) detectDragGestures(
                        onDragStart = { currentPath = Path().apply { moveTo(it.x, it.y) } },
                        onDrag = { change, _ ->
                            change.consume(); currentPath?.lineTo(
                            change.position.x,
                            change.position.y
                        )
                        },
                        onDragEnd = {
                            currentPath?.let {
                                val p = DrawnPath(
                                    it,
                                    activeColor,
                                    15f
                                ); paths.add(p); undoStack.add(p); currentPath = null
                            }
                        }
                    ) else detectTapGestures(onTap = { textTapOffset = it; showTextDialog = true })
                }) {
            canvasSize = IntSize(size.width.toInt(), size.height.toInt())
            originalBitmap?.let { bmp ->
                val img = bmp.asImageBitmap()
                val scale = minOf(size.width / img.width, size.height / img.height)
                drawImage(
                    img,
                    dstOffset = IntOffset(
                        ((size.width - img.width * scale) / 2).toInt(),
                        ((size.height - img.height * scale) / 2).toInt()
                    ),
                    dstSize = IntSize((img.width * scale).toInt(), (img.height * scale).toInt())
                )
                paths.forEach {
                    drawPath(
                        it.path,
                        it.color,
                        style = Stroke(
                            it.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
                currentPath?.let {
                    drawPath(
                        it,
                        activeColor,
                        style = Stroke(15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
                    }
                    texts.forEach {
                        paint.color = it.color.toArgb(); paint.textSize =
                        it.textSize; canvas.nativeCanvas.drawText(
                        it.text,
                        it.position.x,
                        it.position.y,
                        paint
                    )
                    }
                }
            }
        }
    }
}

private fun saveImageWithDrawings(
    context: Context,
    originalBmp: Bitmap,
    paths: List<DrawnPath>,
    texts: List<DrawnText>,
    canvasSize: IntSize
): String? = try {
    val scale = minOf(
        canvasSize.width.toFloat() / originalBmp.width,
        canvasSize.height.toFloat() / originalBmp.height
    )
    val offsetX = (canvasSize.width - originalBmp.width * scale) / 2f
    val offsetY = (canvasSize.height - originalBmp.height * scale) / 2f
    val resultBitmap = originalBmp.copy(Bitmap.Config.ARGB_8888, true)
    val androidCanvas = android.graphics.Canvas(resultBitmap)
        .apply { translate(-offsetX / scale, -offsetY / scale); scale(1f / scale, 1f / scale) }
    val paint = android.graphics.Paint().apply {
        style = android.graphics.Paint.Style.STROKE; strokeJoin =
        android.graphics.Paint.Join.ROUND; strokeCap =
        android.graphics.Paint.Cap.ROUND; isAntiAlias = true
    }
    paths.forEach {
        paint.color = it.color.toArgb(); paint.strokeWidth = it.strokeWidth; androidCanvas.drawPath(
        it.path.asAndroidPath(),
        paint
    )
    }
    val textPaint = android.graphics.Paint()
        .apply { textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true }
    texts.forEach {
        textPaint.color = it.color.toArgb(); textPaint.textSize =
        it.textSize; androidCanvas.drawText(it.text, it.position.x, it.position.y, textPaint)
    }

    val file = File(context.filesDir, "photo_drawn_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out -> resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
    Uri.fromFile(file).toString()
} catch (e: Exception) {
    e.printStackTrace(); null
}