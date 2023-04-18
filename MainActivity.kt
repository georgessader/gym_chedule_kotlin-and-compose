package com.example.gymschedule

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gymschedule.ui.theme.GymScheduleTheme
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


class MainActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymScheduleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0x44B22828)
                ) {
                    RequestStoragePermissions(
                        onPermissionGranted = {
                            granted.value = true
                        },
                        onPermissionDenied = {
                            granted.value = false
                        }
                    )
                    if (granted.value) {
                        LoadData()
                        Tabs()

                    }
                }
            }
        }
    }
}

val granted = mutableStateOf(false)
val i = mutableStateOf(0)
val showDialog = mutableStateOf(false)
val openEditDialog = mutableStateOf(false)
var colors = mapOf("Test" to mutableStateOf(Color.Green)).toMutableMap()
var selectedTabIndex by mutableStateOf(0)
var intent = mutableStateOf(Intent())


@SuppressLint("UnrememberedMutableState")
@Composable
fun LoadData() {
    val f = File(LocalContext.current.filesDir, "data.txt")
    if (f.exists()) {
        val br = BufferedReader(FileReader(f))
        var s = ""
        if (br.readLine() != null) {
            val f = File(LocalContext.current.filesDir, "data.txt")
            s = f.readLines()[0]
            val map = s.split(",,,")
            for (i in 0 until map.count()) {
                if (map[i].contains("=")) {
                    val ss = map[i].split("=")
                    when {
                        ss[1] == "r" -> colors[ss[0]] = mutableStateOf(Color.Red)
                        ss[1] == "y" -> colors[ss[0]] = mutableStateOf(Color.Yellow)
                        ss[1] == "b" -> colors[ss[0]] = mutableStateOf(Color.Green)
                    }
                }
            }
            Log.d("Output data: ", colors.toString())
        }
    }

}

@Composable
fun ImageFromFile(filePath: String, s: String) {
    val context = LocalContext.current
    val imageDirectory = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], s
    )
    val file = File(imageDirectory, filePath)
    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)

    val painter: Painter = if (bitmap != null) {
        BitmapPainter(bitmap.asImageBitmap())
    } else {
        ColorPainter(color = Color.Red)
    }
    Image(
        painter = painter,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(6.dp),
        alignment = Alignment.Center,
        contentDescription = ""
    )
}

@ExperimentalFoundationApi
@SuppressLint("UnrememberedMutableState")
@Composable
fun ImagesTextBox(fileName: String, index: Int, cat: String) {
    val checkDel = remember { mutableStateOf<Boolean>(false) }
    Box(
        modifier = Modifier
            .padding(2.dp)
            .background(Color(0xFFB22828),RoundedCornerShape(16.dp))
            .size(200.dp)
            .combinedClickable(
                onClick = {
                    showDialog.value = true
                    i.value = index
                },
            )
    ) {
        ImageFromFile(fileName, cat)
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(Color(0xFFB22828),RoundedCornerShape(16.dp))
                .alpha(0.9f)
                .padding(4.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = fileName.replace(".jpg", ""),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 10.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Divider(
                color = Color.White,
                thickness = 1.dp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            )
            {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(5.dp)
                        .size(25.dp)
                        .clickable {
                            checkDel.value = true
                        }
                )
                Text(
                    modifier = Modifier
                        .padding(horizontal =  10.dp),
                    text = "⬤",
                    color = colors[cat + fileName]!!.value,
                )
                if (checkDel.value) {
                    openEditDialog.value = true
                    i.value = index
                    checkDel.value = false
                }

            }
        }
    }
}

@Composable
fun UploadBox(dir: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(2.dp)
            .border(4.dp, Color(0xFFB22828),RoundedCornerShape(16.dp))
            .size(200.dp)
    ) {
        IconButton(
            modifier = Modifier
                .fillMaxSize(),

            onClick = {
                intent.value = Intent(context, UploadPhoto::class.java).apply {
                    putExtra("catalog", dir)
                }
                context.startActivity(intent.value)
            }) {
            Icon(
                imageVector = Icons.Outlined.AddCircle,
                contentDescription = "Edit",
                tint = Color(0xFFB22828),
                modifier = Modifier
                    .size(60.dp)
            )
        }

    }
}

@ExperimentalFoundationApi
@Composable
fun catalogs(cats: String) {
    val s = cats
    val context = LocalContext.current
    val imageDirectory = File(
        ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_PICTURES
        )[0], s
    )
    val directory = File(imageDirectory.toString())
    val files = directory.listFiles()

    LazyVerticalGrid(
        cells = GridCells.Fixed(3), // set the number of columns
        contentPadding = PaddingValues(16.dp) // set the padding between items
    ) {
        if (!files.isNullOrEmpty()) {
            items(files.size + 1) { index ->
                if (index == files.size) {
                    UploadBox(s)
                } else {
                    if (showDialog.value) {
                        ChangeColor(s + files[i.value].name)
                        showDialog.value = false
                    }
                    if (!colors.containsKey(s + files[index].name))
                        colors[s + files[index].name] =
                            remember { mutableStateOf(Color.Green) }

                    if (openEditDialog.value) {
                        openEditableActivity(files[i.value].name, s)
                        openEditDialog.value = false
                    }
                    ImagesTextBox(files[index].name, index, s)
                }
            }
        } else {
            items(1) {
                UploadBox(s)
            }
        }
    }
}

@Composable
fun openEditableActivity(name: String, cat: String) {
    val context = LocalContext.current
    intent.value = Intent(context, EditPhoto::class.java).apply {
        putExtra("catalog", cat)
        putExtra("name", name)
    }
    context.startActivity(intent.value)
}

@Composable
fun RequestStoragePermissions(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
}


class PermissionResultCallback(
    private val onPermissionGranted: (Any?, Any?) -> Unit
) : ActivityResultCallback<Map<String, Boolean>> {

    override fun onActivityResult(result: Map<String, Boolean>?) {
        val permissions = result?.keys?.toTypedArray() ?: emptyArray()
        val grantResults =
            result?.values?.map { if (it) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED }
                ?.toIntArray() ?: IntArray(0)
        onPermissionGranted(permissions, grantResults)
    }
}


@ExperimentalFoundationApi
@Composable
fun Tabs() {
    val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
    Column {
        ScrollableTabRow(
            selectedTabIndex,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            tabs.forEachIndexed { index, text ->
                Tab(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFB22828)),
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    })
            }
        }
        when (selectedTabIndex) {
            0 -> {
                catalogs(tabs[0])
            }
            1 -> {
                catalogs(tabs[1])
            }
            2 -> {
                catalogs(tabs[2])
            }
            3 -> {
                catalogs(tabs[3])
            }
            4 -> {
                catalogs(tabs[4])
            }
            5 -> {
                catalogs(tabs[5])
            }
        }
    }
}

@Composable
fun saveColors() {
    var s = ""
    colors.forEach { (key) ->
        s += "$key="
        if (colors[key]!!.value == Color.Red)
            s += "r,,,"
        else if (colors[key]!!.value == Color.Yellow)
            s += "y,,,"
        else
            s += "b,,,"
    }
//    s = ""
    val file = File(LocalContext.current.filesDir, "data.txt")
    file.writeText(s)
}

@Composable
fun ChangeColor(s: String) {
    when (colors[s]!!.value) {
        Color.Red -> colors[s]!!.value = Color.Yellow
        Color.Yellow -> colors[s]!!.value = Color.Green
        else -> colors[s]!!.value = Color.Red
    }
    saveColors()
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GymScheduleTheme {
        Tabs()
    }
}