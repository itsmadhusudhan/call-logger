package com.shreekaram.calllogger.screens.insights

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun InsightsScreens(navHostController: NavHostController) {
    Scaffold {
        Column {
            Text(text = "Insights Screen")
        }
    }
}
