package com.shreekaram.calllogger.navigation

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shreekaram.calllogger.screens.home.HomeScreen
import com.shreekaram.calllogger.screens.insights.InsightsScreens

@Composable
fun RootNavigationGraph(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = Route.Root.id) {
        composable(Route.Root.id) {
            RootScreen( navHostController)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RootScreen(navHostController: NavHostController) {
    val homeNavController = rememberNavController()

    Scaffold(bottomBar = { BottomNavigationBar(navHostController = homeNavController) }) {
        NavHost(
            startDestination = Route.Home.id,
            navController = homeNavController,
        ) {
            composable(Route.Home.id) {
                HomeScreen(homeNavController)
            }

            composable(Route.Insights.id) {
                InsightsScreens(homeNavController)
            }
        }
    }
}


