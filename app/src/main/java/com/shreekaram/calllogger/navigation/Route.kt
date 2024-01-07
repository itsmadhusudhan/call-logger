package com.shreekaram.calllogger.navigation

sealed class Route(var id: String, var title: String, var args: String = "") {
    data object Root : Route("root", "Root")
    data object Home : Route("home", "Home")
    data object Insights : Route("insights", "Insights")
}