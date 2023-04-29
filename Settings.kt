package com.example.gymschedule

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.gymschedule.ui.theme.GymScheduleTheme
import org.apache.commons.io.FileUtils.deleteDirectory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Settings : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context= LocalContext.current
            val activity = context as Activity
            GymScheduleTheme("blue") {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    layou()
                    activity.window.statusBarColor = ContextCompat.getColor(context, barcolor.value)
                    activity.window.navigationBarColor = ContextCompat.getColor(context, barcolor.value)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun layou() {
    val st = remember { mutableStateOf("") }
    val datatext = remember { mutableStateOf("") }
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val mode = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(d2Color.value),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Change Theme Color:",
            fontSize = 20.sp,
        )
        Row(
            modifier = Modifier.padding(20.dp)
        )
        {
            val context = LocalContext.current
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable {
                        changeTheme(context, "green")
                    },
                text = "⬤",
                fontSize = 40.sp,
                color = Color(0xFF019131)
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable {
                        changeTheme(context, "blue")
                    },
                text = "⬤",
                fontSize = 40.sp,
                color = Color(0xFF429ef5)
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable {
                        changeTheme(context, "brown")
                    },
                text = "⬤",
                fontSize = 40.sp,
                color = Color(0xFFB22828)
            )
        }
        Divider(
            color = Color.White,
            thickness = 1.dp
        )
        Text(
            modifier = Modifier
                .padding(16.dp),
            text = "Backup and restore your data"
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = d4Color.value,
                contentColor = Color.White),
            onClick = {
            st.value = backupData(context)
        }) {
            Text("BackUp")
        }
        Spacer(modifier = Modifier.height(16.dp))

        val destdir = ContextCompat.getExternalFilesDirs(
            context,
            Environment.DIRECTORY_DOCUMENTS
        )[0].toString()
        val datas = File(destdir)
        datas.listFiles()?.forEach { file ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            )
            {
                Text(text = file.name.replace("Backup ", ""))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = d4Color.value,
                        contentColor = Color.White),
                    modifier = Modifier.padding(5.dp),
                    onClick = {
                        mode.value = "restore"
                        openDialog.value = true
                        datatext.value = file.name
                    }) {
                    Text("Restore")
                }

                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFB22828),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            mode.value = "delete"
                            openDialog.value = true
                            datatext.value = file.name
                        }
                )
            }
        }
        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                title = {
                    if (mode.value == "restore")
                        Text(text = "Restore Data")
                    else if (mode.value == "delete")
                        Text(text = "Delete")
                },
                text = {
                    if (mode.value == "restore")
                        Text(
                            "Are you sure you want to restore ${
                                datatext.value.replace(
                                    "Backup",
                                    ""
                                )
                            } data?"
                        )
                    else if (mode.value == "delete")
                        Text(
                            "Are you sure you want to delete ${
                                datatext.value.replace(
                                    "Backup",
                                    ""
                                )
                            } data?"
                        )
                },
                buttons = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            modifier = Modifier
                                .padding(5.dp),
                            onClick = { openDialog.value = false }
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            modifier = Modifier
                                .padding(5.dp),
                            onClick = {
                                if (mode.value == "restore")
                                    st.value = restoreData(context, datatext.value)
                                else if (mode.value == "delete")
                                    deleteFileBackup(context, datatext.value)
                                openDialog.value = false
                            }
                        ) {
                            Text("Yes")
                        }
                    }
                }
            )
        }
//        Text(text = st.value)
    }
}

fun changeTheme(context: Context, s: String) {
    barcol.value=s
    when (barcol.value) {
        "green" -> {
            barcolor.value=R.color.green
            d1Color.value = Color(0x44019131)
            d2Color.value = Color(0x66019131)
            d3Color.value = Color(0xBB019131)
            d4Color.value = Color(0xFF019131)
        }
        "brown" -> {
            barcolor.value=R.color.brown
            d1Color.value = Color(0x44B22828)
            d2Color.value = Color(0x66B22828)
            d3Color.value = Color(0xBBB22828)
            d4Color.value = Color(0xFFB22828)
        }
        "blue" -> {
            barcolor.value=R.color.blue
            d1Color.value = Color(0x44429ef5)
            d2Color.value = Color(0x66429ef5)
            d3Color.value = Color(0xBB429ef5)
            d4Color.value = Color(0xFF429ef5)
        }

    }
    val file = File(context.filesDir, "theme.txt")
    file.writeText(s)
}

fun deleteFileBackup(context: Context, name: String) {
    val filedir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_DOCUMENTS
    )[0].toString()
    val file = File(filedir, name)
    deleteDirectory(file)
}

fun restoreData(context: Context, filename: String): String {
    val sourcedir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_DOCUMENTS
    )[0].toString()

    val source = File(sourcedir, filename)

    val destdir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_PICTURES
    )[0].toString()

    val destination = File(destdir)

    if (!destination.exists()) {
        destination.mkdir()
    } else {
        deleteDirectory(destination)
        destination.mkdir()
    }

    if (source.exists()) {
        source.listFiles()?.forEach { file ->
            Log.d("ddd", file.name)
            if (file.isDirectory) {
                val dest = File(destdir, file.name)
                if (!dest.exists())
                    dest.mkdir()
                val subsource = File("$sourcedir/$filename", file.name)
                subsource.listFiles()?.forEach { ff ->
                    val inputStream = FileInputStream(ff)
                    val outputStream = FileOutputStream(File(dest, ff.name))
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                }
            }
        }

        val colordata = File(source, "data.txt")
        var inputStream = FileInputStream(colordata)
        var outputStream = FileOutputStream(File(context.filesDir, colordata.name))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val posdata = File(source, "posdata.txt")
        inputStream = FileInputStream(posdata)
        outputStream = FileOutputStream(File(context.filesDir, posdata.name))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        return "Data: ${filename.replace("Backup ", "")} Restored"
    } else {
        return "Error restoring data: file no found"
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun backupData(context: Context): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateandtime = LocalDateTime.now().format(formatter)

    val sourcedir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_PICTURES
    )[0].toString()

    val source = File(sourcedir)

    val destdir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_DOCUMENTS
    )[0].toString()

    val filecount = File(destdir)
    if (filecount.listFiles()!!.count() >= 5) {
        return "you passed the limit: 5"
    } else {
        val destination = File(destdir, "Backup $dateandtime")

        if (!destination.exists()) {
            destination.mkdir()
        }

        source.listFiles()?.forEach { file ->
            Log.d("ddd", file.name)
            if (file.isDirectory) {
                val dest = File("$destdir/Backup $dateandtime", file.name)
                if (!dest.exists())
                    dest.mkdir()
                val subsource = File(sourcedir, file.name)
                subsource.listFiles()?.forEach { ff ->
                    val inputStream = FileInputStream(ff)
                    val outputStream = FileOutputStream(File(dest, ff.name))
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                }
            }
        }
        val colordata = File(context.filesDir, "data.txt")
        var inputStream = FileInputStream(colordata)
        var outputStream = FileOutputStream(File(destination, colordata.name))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val posdata = File(context.filesDir, "posdata.txt")
        inputStream = FileInputStream(posdata)
        outputStream = FileOutputStream(File(destination, posdata.name))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        return "Done"
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    GymScheduleTheme("blue") {
        layou()
    }
}