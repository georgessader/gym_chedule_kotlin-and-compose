package com.example.myapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapp.ui.theme.ui.theme.MyappTheme
import java.io.File
import java.io.FileOutputStream

class EditPhoto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Firste()
                }
            }
        }
    }
}

var directory = intent.value.getStringExtra("catalog")
var filename = intent.value.getStringExtra("name")
val bitmapedit = mutableStateOf<Bitmap?>(null)

@Composable
fun getBitmapFromUri(d: String?, n: String?): Bitmap? {
    val context = LocalContext.current
    val imageDirectory = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], d
    )
    try {
        val file = File(imageDirectory, n)
        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        return bitmap
    } catch (e: Exception) {
        Log.e(TAG, "Error loading bitmap from path", e)
    }
    return null
}


@Composable
fun DropDownLl(s: String?): String {
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

@Composable
fun deleteFile(od: String?, on: String?) {
    val context = LocalContext.current
    val imageDirectory = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], od
    )
    val file = File(imageDirectory, on)
    file.delete()
    colors.remove(od+on)
    saveColors()
    context.startActivity(Intent(context, MainActivity::class.java))
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun editFile(od: String?, on: String?, n: String?, catchanged: String) {
    val context = LocalContext.current
    val imageDirectory1 = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], od
    )

    var ename by remember { mutableStateOf(n) }
    if (ename != "") {
        val context = LocalContext.current
        val imageDirectory = File(
            ContextCompat.getExternalFilesDirs(
                context,
                Environment.DIRECTORY_PICTURES
            )[0], catchanged
        )
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir()
        }
        var imageFile = File(imageDirectory, "$ename.jpg")
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }
        val outputStream = FileOutputStream(imageFile)
        bitmapedit.value!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val file1 = File(imageDirectory1, on)
        file1.delete()

        colors["$catchanged$n.jpg"]=mutableStateOf(colors[od+on]!!.value)
        
        colors.remove(od+on)
        saveColors()
        context.startActivity(Intent(context, MainActivity::class.java))
    } else {
        Text(text = "Empty Exercise name")
    }
}

@Composable
fun Firste() {
    directory = intent.value.getStringExtra("catalog")
    filename = intent.value.getStringExtra("name")
    bitmapedit.value = getBitmapFromUri(directory, filename)
    var change = remember { mutableStateOf<Boolean>(false) }
    var delete = remember { mutableStateOf<Boolean>(false) }
    var ename by remember { mutableStateOf(filename) }

    var up = remember { mutableStateOf<Boolean>(false) }
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
                bitmapedit.value?.let {
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
        var changedCat = DropDownLl(directory)
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
                    change.value = true
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
                    delete.value = true
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
        if (change.value) {
            editFile(directory, filename, ename, changedCat)
            change.value = false
        }
        if (delete.value) {
            deleteFile(directory, filename)
            delete.value = false
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    MyappTheme {
        Firste()
    }
}