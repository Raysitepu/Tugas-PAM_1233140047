package com.itera.myprofile

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "My Profile App",
        state = rememberWindowState(width = 480.dp, height = 840.dp),
    ) {
        window.minimumSize = Dimension(360, 600)
        App()
    }
}
