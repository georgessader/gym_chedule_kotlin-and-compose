package com.example.gymschedule

import android.Manifest
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
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPagerApi::class)
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
//                        clearData()
                        TodoIntent()
                        LoadData()
                        LoadDataPos()
//                        Tabs()
                        TabLayout()
                    }
                }
            }
        }
    }
}

val granted = mutableStateOf(false)
var colors = mapOf("Test" to mutableStateOf(Color.Green)).toMutableMap()
var position = mapOf("Test" to mutableStateOf("")).toMutableMap()
var intent = mutableStateOf(Intent())
var mode = ""
val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
var extDir=""


@OptIn(ExperimentalUnitApi::class)
@ExperimentalPagerApi
@Composable
fun TabLayout() {
    val pagerState = rememberPagerState(0)
    Column{
        Tabs(pagerState = pagerState)
        TabsContent(pagerState = pagerState)
    }
}


@ExperimentalPagerApi
@Composable
fun Tabs(pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    ScrollableTabRow(
        edgePadding = 0.dp,
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
            )
        }
    ) {
        tabs.forEachIndexed { index, _ ->
            Tab(
//                icon = {
//                    Icon(imageVector = list[index].second, contentDescription = null)
//                },
                text = {
                    Text(
                        tabs[index],
                        color = if (pagerState.currentPage == index) Color.White else Color.LightGray
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalPagerApi
@Composable
fun TabsContent(pagerState: PagerState) {
    val context = LocalContext.current
    HorizontalPager(
        count = tabs.count(),
        state = pagerState
    ) { page ->
        Catalogs(context, tabs[page])
    }
}




@Composable
fun TodoIntent() {
    val context= LocalContext.current
    extDir=ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_PICTURES
    )[0].toString()


    mode = intent.value.getStringExtra("mode").toString()
    if (mode != "") {
        if (mode == "addpos") {
            val dirfname = intent.value.getStringExtra("dirfname")
            val vall = intent.value.getStringExtra("value")
            dirfname?.let {
                position[dirfname] = mutableStateOf(vall.toString())
                SavePosition()
                intent.value.removeExtra("dirfname")
                intent.value.removeExtra("value")
            }
        } else if (mode == "editpos") {
            val odirfname = intent.value.getStringExtra("odirfname")
            val ndirfname = intent.value.getStringExtra("ndirfname")
            val vall = intent.value.getStringExtra("value")
            ndirfname?.let {
                position.remove(odirfname.toString().replace(".jpg", ""))
                position[ndirfname] = mutableStateOf(vall.toString())
                SavePosition()


                colors["$ndirfname.jpg"] =
                    mutableStateOf(colors[odirfname.toString()]?.value ?: Color.Green)
                if (odirfname != "$ndirfname.jpg") {
                    colors.remove(odirfname)
                }

                SaveColors()
                intent.value.removeExtra("odirfname")
                intent.value.removeExtra("ndirfname")
                intent.value.removeExtra("value")
            }
        } else if (mode == "deletepos") {
            val dirfname = intent.value.getStringExtra("dirfname")
            position.remove(dirfname!!.replace(".jpg", ""))
            colors.remove(dirfname)
            SaveColors()
            SavePosition()
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun LoadData() {
    val f = File(LocalContext.current.filesDir, "data.txt")
    if (f.exists()) {
        val br = BufferedReader(FileReader(f))
        val s: String
        if (br.readLine() != null) {
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
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun LoadDataPos() {
    val f = File(LocalContext.current.filesDir, "posdata.txt")
    if (f.exists()) {
        val br = BufferedReader(FileReader(f))
        val s: String
        if (br.readLine() != null) {
            s = f.readLines()[0]
            val map = s.split(",,,")
            for (i in 0 until map.count()) {
                if (map[i].contains("=")) {
                    val ss = map[i].split("=")
                    position[ss[0]] = mutableStateOf(ss[1])
                }
            }
        }
    }
}

@Composable
fun ImageFromFile(file: File, s: String) {
    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)

    val painter: Painter = if (bitmap != null) {
        BitmapPainter(bitmap.asImageBitmap())
    } else {
        ColorPainter(color = Color.Red)
    }
    var zoom = 1.0f
    var offsetx = 0.0f
    var offsety = 0.0f
    val filekey = s + file.name.replace(".jpg", "")
    if (position.containsKey(filekey)) {
        zoom = position[filekey]!!.value.split(" and ")[0].toFloat()
        offsetx = position[filekey]!!.value.split(" and ")[1].split(",")[0].toFloat()
        offsety = position[filekey]!!.value.split(" and ")[1].split(",")[1].toFloat()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)

    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            elevation = 8.dp
        ) {
            Image(
                painter = painter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFB22828))
                    .padding(6.dp)
                    .width(100.dp)
                    .height(100.dp)
                    .graphicsLayer(
                        scaleX = zoom * 1.05f,
                        scaleY = zoom * 1.05f,
                        translationX = offsetx / 3.6f,
                        translationY = offsety / 3.6f
                    )
                    .clickable {
                        val st = s + file.name
                        changeColor(st)
                    },
                alignment = Alignment.Center,
                contentDescription = ""
            )
        }
    }
}


@ExperimentalFoundationApi
@SuppressLint("UnrememberedMutableState")
@Composable
fun ImagesTextBox(cont: Context, file: File, cat: String) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .background(Color(0xFFB22828), RoundedCornerShape(16.dp)),
    ) {
//        SaveColors()
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .alpha(0.9f)
        )
        {
            ImageFromFile(file, cat)
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .background(Color(0xFFB22828), RoundedCornerShape(16.dp))
//                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = file.name.replace(".jpg", ""),
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
                                openEditableActivity(cont, file.name, cat)
                            }
                    )
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 10.dp),
                        text = "⬤",
                        color = colors[cat + file.name]!!.value,
                    )
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
            .border(4.dp, Color(0xFFB22828), RoundedCornerShape(16.dp))
            .size(180.dp)
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
fun Catalogs(context: Context, cats: String) {

    val directory = File(extDir,cats)
    val files = directory.listFiles()

    LazyVerticalGrid(
        cells = GridCells.Fixed(3), // set the number of columns
        contentPadding = PaddingValues(16.dp) // set the padding between items
    ) {
        if (!files.isNullOrEmpty()) {
            items(files.size + 1) { index ->
                if (index == files.size) {
                    UploadBox(cats)
                } else {
                    if (!colors.containsKey(cats + files[index].name))
                        colors[cats + files[index].name] =
                            remember { mutableStateOf(Color.Green) }

                    ImagesTextBox(context, files[index], cats)
                }
            }
        } else {
            items(1) {
                UploadBox(cats)
            }
        }
    }
//    Divider()
//    Text(colors.keys.toString())
//    Divider()
//    Text(position.keys.toString())
}

fun openEditableActivity(context: Context, name: String, cat: String) {
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
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { p ->
        val granted = p.all { it.value }
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
//
//@ExperimentalPagerApi // 1.
//@Preview
//@Composable
//fun TabsWithSwiping() {
//    var tabIndex by remember { mutableStateOf(0) }
//    val tabTitles = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
////    val tabTitles = listOf("Hello", "There", "World")
//    val pagerState = rememberPagerState() // 2.
//    Column {
//        ScrollableTabRow(selectedTabIndex = tabIndex,
//            indicator = { tabPositions -> // 3.
//                TabRowDefaults.Indicator(
//                    Modifier.pagerTabIndicatorOffset(
//                        pagerState,
//                        tabPositions
//                    )
//                )
//            }) {
//            tabTitles.forEachIndexed { index, title ->
//                Tab(selected = tabIndex == index,
//                    onClick = { tabIndex = index },
//                    text = { Text(text = title) })
//            }
//        }
//        HorizontalPager(
//            // 4.
//            count = tabTitles.size,
//            state = pagerState,
//        ) { tabIndex ->
//            Text(
//                tabIndex.toString(),
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.White)
//            )
//        }
//    }
//}
//
//@ExperimentalPagerApi // 1.
//@Preview
//@ExperimentalFoundationApi
//@Composable
//fun Tabs2() {
//    val context = LocalContext.current
//    val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
//    var selectedTabIndex by remember { mutableStateOf(0) }
//    val pagerState = rememberPagerState()
//    Column(
//        verticalArrangement = Arrangement.Top
//    ) {
//        ScrollableTabRow(
//            selectedTabIndex,
//            edgePadding = 0.dp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .verticalScroll(rememberScrollState()),
//            indicator = { tabPositions ->
//                TabRowDefaults.Indicator(
//                    Modifier.pagerTabIndicatorOffset(
//                        pagerState,
//                        tabPositions
//                    )
//                )
//            }
//        ) {
//            tabs.forEachIndexed { index, text ->
//                Tab(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color(0xFFB22828)),
//                    selected = selectedTabIndex == index,
//                    onClick = { selectedTabIndex = index },
//                    text = {
//                        Text(
//                            text = text,
//                            fontSize = 14.sp,
//                            overflow = TextOverflow.Ellipsis,
//                            maxLines = 1,
//                        )
//                    })
//            }
//        }
//        HorizontalPager(
//            count = tabs.size,
//            state = pagerState,
//        ) { tabIndex ->
//            selectedTabIndex=tabIndex
////            Catalogs(context, tabs[tabIndex])
//        }
//    }
//}


@ExperimentalPagerApi // 1.
@Preview
@ExperimentalFoundationApi
@Composable
fun Tabs() {
    val context = LocalContext.current
    val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
    var selectedTabIndex by remember { mutableStateOf(0) }
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
        Catalogs(context, tabs[selectedTabIndex])
    }
}

@Composable
fun SavePosition() {
    var s = ""
    position.forEach { (key) ->
        s += "$key="
        s += position[key]!!.value + ",,,"
    }
//    s = ""
    val file = File(LocalContext.current.filesDir, "posdata.txt")
    file.writeText(s)
}


@Composable
fun SaveColors() {
    var s = ""
    colors.forEach { (key) ->
        s += "$key="
        s += when (colors[key]!!.value) {
            Color.Red -> "r,,,"
            Color.Yellow -> "y,,,"
            else -> "b,,,"
        }
    }
//    s = ""
    val file = File(LocalContext.current.filesDir, "data.txt")
    file.writeText(s)
}

@Composable
fun clearData() {
    val s = ""
    val file = File(LocalContext.current.filesDir, "data.txt")
    file.writeText(s)
    val fil = File(LocalContext.current.filesDir, "posdata.txt")
    fil.writeText(s)
}

fun changeColor(s: String) {
    when (colors[s]!!.value) {
        Color.Red -> colors[s]!!.value = Color.Yellow
        Color.Yellow -> colors[s]!!.value = Color.Green
        else -> colors[s]!!.value = Color.Red
    }
}

@ExperimentalPagerApi // 1.
@Preview
@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GymScheduleTheme {

    }
}