package com.example.gymschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymschedule.ui.theme.GymScheduleTheme
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class EditPhoto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymScheduleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0x44B22828)
                ) {
                    FirstFun()
                }
            }
        }
    }
}

var directory = intent.value.getStringExtra("catalog")
var filename = intent.value.getStringExtra("name")
val bitmap_edit = mutableStateOf<Bitmap?>(null)

@Composable
fun getBitmapFromUri(d: String, n: String): Bitmap? {
    val context = LocalContext.current
    val imageDirectory = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], d
    )
    try {
        val file = File(imageDirectory, n)
        return BitmapFactory.decodeFile(file.absolutePath)
    } catch (e: Exception) {
        Log.e(TAG, "Error loading bitmap from path", e)
    }
    return null
}


@Composable
fun dropDownLl(s: String?): String {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(s) }
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

fun deleteFile(context: Context, od: String, on: String) {
    val imageDirectory = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], od
    )
    val file = File(imageDirectory, on)
    file.delete()
    colors.remove(od + on)
    context.startActivity(Intent(context, MainActivity::class.java))
}

@SuppressLint("UnrememberedMutableState")
fun editFile(context: Context, od: String, on: String, n: String, cat_changed: String): String {
    val imageDirectory1 = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], od
    )

    val ename by mutableStateOf(n)
    var result by mutableStateOf("")
    if (ename != "") {
        val imageDirectory = File(
            ContextCompat.getExternalFilesDirs(
                context,
                Environment.DIRECTORY_PICTURES
            )[0], cat_changed
        )
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir()
        }
        val imageFile = File(imageDirectory, "$ename.jpg")
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }
        val outputStream = FileOutputStream(imageFile)
        bitmap_edit.value!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val file1 = File(imageDirectory1, on)
        file1.delete()

        colors["$cat_changed$n.jpg"] = mutableStateOf(colors[od + on]!!.value)
        colors.remove(od + on)
        context.startActivity(Intent(context, MainActivity::class.java))
    } else {
        result = "Empty Exercise name"
    }
    return result
}

@Composable
fun FirstFun() {
    directory = intent.value.getStringExtra("catalog")
    filename = intent.value.getStringExtra("name")
    bitmap_edit.value = getBitmapFromUri(directory.toString(), filename.toString())
    var ename by remember { mutableStateOf(filename) }

    val context = LocalContext.current

    var s by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(250.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                elevation = 8.dp
            ) {
                bitmap_edit.value?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .height(120.dp)
                    )
                }
            }
        }
        val changedCat = dropDownLl(directory)
        TextField(
            value = ename.toString().replace(".jpg", ""),
            onValueChange = { ename = it },
            label = { Text("Exercise name") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )
        Row()
        {
            IconButton(
                onClick = {
                    s = editFile(
                        context, directory.toString(), filename.toString(),
                        ename.toString(), changedCat
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Click to share",
                    tint = Color.Blue,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(8.dp)
                )
            }
            IconButton(
                onClick = {
                    deleteFile(context, directory.toString(), filename.toString())
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Click to share",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(8.dp)
                )
            }
        }
        Text(text = s)
        SaveColors()
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    GymScheduleTheme {
        FirstFun()
    }
}