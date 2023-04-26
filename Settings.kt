package com.example.gymschedule

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.gymschedule.ui.theme.GymScheduleTheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class Settings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymScheduleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    layou()
                }
            }
        }
    }
}

@Composable
fun layou() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0x55B22828)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp),
            text = "Backup and restore your data"
        )
        Button(onClick = {
//            backupdata(context)
        }) {
            Text("BackUp")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { restoredata(context) }) {
            Text("Restore")
        }
    }
}


fun restoredata(context: Context) {
    val sourcedir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_PICTURES
    )[0].toString()

    val source = File(sourcedir, "Backup 1")


    val destdir = ContextCompat.getExternalFilesDirs(
        context,
        Environment.DIRECTORY_DOCUMENTS
    )[0].toString()

    val destination = File(destdir, "Backup 1")

    if (!destination.exists()) {
        destination.mkdir()
    }


    source.listFiles()?.forEach { file ->
        if(file.isDirectory)
        {
            destination.mkdir()
            val f=File(sourcedir,file.absolutePath)
            val dest = File(destdir, file.name)
            f.listFiles()?.forEach { ff ->
                val inputStream = FileInputStream(ff)
                val outputStream = FileOutputStream(File(dest, ff.name))
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
        }
        val inputStream = FileInputStream(file)
        val outputStream = FileOutputStream(File(destination, file.name))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    GymScheduleTheme {
        layou()
    }
}