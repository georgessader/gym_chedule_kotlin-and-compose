package com.example.gymschedule

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gymschedule.ui.theme.GymScheduleTheme
import java.io.File
import java.io.FileOutputStream

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput


class UploadPhoto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymScheduleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0x44B22828)
                ) {
//                    TopBar("Create new exercise")
                    First()
                }
            }
        }
    }
}

val bitmap = mutableStateOf<Bitmap?>(null)
var direc = intent.value.getStringExtra("catalog")


@SuppressLint("UnrememberedMutableState")
@Composable
fun ImagePicker() {
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    imageUri?.let {
        if (Build.VERSION.SDK_INT < 28) {
            bitmap.value = MediaStore.Images
                .Media.getBitmap(context.contentResolver, it)

        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver, it)
            bitmap.value = ImageDecoder.decodeBitmap(source)
        }
    }


    val result = remember { mutableStateOf<Bitmap?>(null) }
    val launcherr =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            result.value = it
        }


    Row(
        modifier = Modifier.padding(16.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Upload",
            tint = Color(0xFFB22828),
            modifier = Modifier
                .padding(5.dp)
                .padding(horizontal = 10.dp)
                .size(40.dp)
                .clickable { launcher.launch("image/*") }
        )
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Camera",
            tint = Color(0xFFB22828),
            modifier = Modifier
                .padding(5.dp)
                .padding(horizontal = 10.dp)
                .size(40.dp)
                .clickable {
                    launcherr.launch()
                }
        )
        result.value?.let { image ->
            bitmap.value = image
        }
    }
}


@Composable
fun dropDownL(): String {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(direc) }
    Box(modifier = Modifier.width(200.dp)) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedItem?.let { Text(it) }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
            for (tab in tabs) {
                DropdownMenuItem(onClick = {
                    selectedItem = tab
                    expanded = false
                }) {
                    Text(tab)
                }
            }
        }
    }
    return selectedItem.toString()
}

@Composable
fun First() {
    direc = intent.value.getStringExtra("catalog")
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap.value != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .padding(6.dp)

            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 8.dp
                ) {
                    val maxZoom = 5f
                    val minZoom = 0.2f
                    bitmap.value?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFB22828))
                                .padding(6.dp)
                                .width(300.dp)
                                .height(300.dp)
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, c, zoomAmount, _ ->
                                        zoom *= zoomAmount
                                        offset += c
                                        zoom = zoom.coerceIn(minZoom, maxZoom)
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = zoom,
                                    scaleY = zoom,
                                    translationX = offset.x,
                                    translationY = offset.y,
                                )
                        )
//                        SavePosition()
                    }
//                    Log.d("dddddddddddddddddddddd", "$zoom ,,,, $offset")
                }
            }
        }

        ImagePicker()
        val context = LocalContext.current
        val s: String = dropDownL()
        var ename by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        TextField(
            value = ename,
            onValueChange = { ename = it },
            label = { Text("Exercise name") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )
        Button(
            onClick = {
                if (bitmap.value != null && ename != "") {
                    val imageDirectory = File(
                        ContextCompat.getExternalFilesDirs(
                            context,
                            Environment.DIRECTORY_PICTURES
                        )[0], s
                    )
                    if (!imageDirectory.exists()) {
                        imageDirectory.mkdir()
                    }
                    var imageFile = File(imageDirectory, "$ename.jpg")
                    if (!imageFile.exists()) {
                        imageFile.createNewFile()
                    } else {
                        var c = 1
                        while (imageFile.exists()) {
                            imageFile = File(imageDirectory, "$ename $c.jpg")
                            if (!imageFile.exists()) {
                                imageFile.createNewFile()
                                break
                            }
                            c++
                        }

                    }
                    val outputStream = FileOutputStream(imageFile)
                    bitmap.value!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    intent.value = Intent(context, MainActivity::class.java).apply {
                        putExtra("mode","addpos")
                        putExtra("dirfname", s+ename)
                        putExtra("value", "$zoom and ${offset.x},${offset.y}")
                    }
                    context.startActivity(intent.value)

                } else {
                    result = if (bitmap.value == null)
                        "No Photo"
                    else
                        "Empty Exercise name"
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Add")
        }
        Text(text = result)
//        SavePosition()
    }
}

//@Composable
//fun createBitmapFromImage(image: Image): Bitmap {
//    val bitmapPainter =
//        image as? BitmapPainter ?: image.asImageBitmap().asAndroidBitmap().let(::BitmapPainter)
//    return bitmapPainter.bitmap
//}


@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    GymScheduleTheme {
        First()
    }
}