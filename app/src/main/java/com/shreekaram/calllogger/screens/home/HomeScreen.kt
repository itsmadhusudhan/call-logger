package com.shreekaram.calllogger.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.shreekaram.calllogger.CallLogWorker


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(navHostController: NavHostController) {
    val context = LocalContext.current

    fun saveCallLog() {
        val constraints = Constraints.Builder()
            .build()

        val workRequest = OneTimeWorkRequestBuilder<CallLogWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    Scaffold {
        Column {
            Text(text = "Home Screen")
            Button(onClick = {
                saveCallLog()
            }) {
                Text("Access Call logs")
            }
        }
    }
}
