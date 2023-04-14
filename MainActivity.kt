package com.example.myapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.theme.MyappTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    loadData()
                    tabs()
                }
            }
        }
    }
}

var colors = mapOf("Test" to mutableStateOf(Color.Black)).toMutableMap()
var colorsCode = mapOf(
    "r" to mutableStateOf(Color.Red),
    "y" to mutableStateOf(Color.Yellow),
    "b" to mutableStateOf(Color.Black)
).toMutableMap()


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun loadData()
{
    val f = File(LocalContext.current.filesDir, "data.txt")
    val s=f.readLines()[0]
    val map = s.split(",,,")
    for (i in 0..map.count()-1)
    {
        if(map[i].contains("="))
        {
            val ss=map[i].split("=")
            if(ss[1]=="r")
                colors[ss[0]]=mutableStateOf(Color.Red)
            else if(ss[1]=="y")
                colors[ss[0]]=mutableStateOf(Color.Yellow)
            else if(ss[1]=="b")
                colors[ss[0]]=mutableStateOf(Color.Black)
        }

    }
    Log.d("dddddddddddddddddd", colors.toString())
}

@Composable
fun ImageFromFile(filePath: String) {


    val file = File(filePath)
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
            .padding(6.dp)
            .height(120.dp), //Remove the offset here
        alignment = Alignment.Center,
        contentDescription = ""
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun tabs() {
    val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
    var selectedTabIndex by remember { mutableStateOf(0) }
    Column() {
        ScrollableTabRow(
            selectedTabIndex,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            tabs.forEachIndexed { index, text ->
                Tab(
                    modifier = Modifier.fillMaxWidth(),
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
                val directory = File("/storage/emulated/0/gymimages")
                val files = directory.listFiles()
                val showDialog = mutableStateOf(false)
                val i = mutableStateOf(0)

                LazyVerticalGrid(
                    cells = GridCells.Fixed(3), // set the number of columns
                    contentPadding = PaddingValues(16.dp) // set the padding between items
                ) {

                    items(files.size) { index ->
                        if(showDialog.value) {
                            changeColor(files[i.value].name);
                            showDialog.value = false
                        }
                        if(!colors.containsKey(files[index].name) )
                            colors[files[index].name] = remember { mutableStateOf(Color.Black) }
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .background(colors[files[index].name]!!.value)
                                .size(150.dp)
                                .clickable(
                                    onClick = {
                                        showDialog.value = true
                                        i.value=index
                                    }
                                )
                        ) {
                            val s = files[index].name;
                            ImageFromFile("/storage/emulated/0/gymimages/$s")
                            Column(
                                verticalArrangement = Arrangement.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .alpha(0.9f)
                                    .padding(4.dp)
                                    .align(Alignment.BottomStart)
                            ) {
                                Text(
                                    text = s.replace(".jpg", ""),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun changeColor(s: String) {
    if (colors[s]!!.value == Color.Red)
        colors[s]!!.value = Color.Yellow
    else if (colors[s]!!.value == Color.Yellow)
        colors[s]!!.value = Color.Black
    else
        colors[s]!!.value = Color.Red

    var s=""
    colors.forEach { (key, value) ->
        s+="$key="
        if (colors[key]!!.value == Color.Red)
            s+="r,,,"
        else if (colors[key]!!.value == Color.Yellow)
            s+="y,,,"
        else
            s+="b,,,"
    }
    val file = File(LocalContext.current.filesDir, "data.txt")
    file.writeText(s)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyappTheme {
        tabs()
    }
}