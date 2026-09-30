package com.itera.myprofile

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfileUiTest {
    @Test
    fun profileShowsContactAfterEntranceAnimation() = runSkikoComposeUiTest(size = Size(480f, 820f)) {
        setContent { App() }
        onNodeWithText("Ray Regan Sitepu").assertIsDisplayed()
        onNodeWithText("ray.123140047@student.itera.ac.id").assertIsDisplayed()
        onNodeWithText("Edit Profile").assertIsDisplayed()
        saveScreenshot("desktop-profile.png")
    }

    @Test
    fun editValidatesSavesAndCancels() = runSkikoComposeUiTest(size = Size(1100f, 850f)) {
        setContent { App() }
        onNodeWithText("Edit Profile").performClick()
        onNode(hasSetTextAction() and hasText("Nama")).performTextReplacement("")
        onNodeWithText("Simpan").assertIsNotEnabled()
        onNode(hasSetTextAction() and hasText("Nama")).performTextReplacement("Ray Regan")
        onNode(hasSetTextAction() and hasText("Email")).performTextReplacement("email-salah")
        onNodeWithText("Simpan").assertIsNotEnabled()
        onNode(hasSetTextAction() and hasText("Email")).performTextReplacement("ray@example.com")
        onNodeWithText("Simpan").assertIsEnabled().performClick()
        onNodeWithText("Ray Regan").assertIsDisplayed()
        onNodeWithText("ray@example.com").assertIsDisplayed()

        onNodeWithText("Edit Profile").performClick()
        onNode(hasSetTextAction() and hasText("Nama")).performTextReplacement("Tidak disimpan")
        onNodeWithText("Batal").performClick()
        onNodeWithText("Ray Regan").assertIsDisplayed()
        onNodeWithText("Tidak disimpan").assertDoesNotExist()
    }

    @Test
    fun narrowLayoutCanScrollToContact() = runSkikoComposeUiTest(size = Size(390f, 844f)) {
        setContent { App() }
        onNodeWithText("Ray Regan Sitepu").assertIsDisplayed()
        onNodeWithText("ray.123140047@student.itera.ac.id").performScrollTo().assertIsDisplayed()
        onNodeWithText("Location").performScrollTo().assertIsDisplayed()
        onNodeWithText("Edit Profile").performScrollTo().assertIsDisplayed()
    }
}

@OptIn(ExperimentalTestApi::class)
private fun SemanticsNodeInteractionsProvider.saveScreenshot(filename: String) {
    val bitmap = onRoot().captureToImage().asSkiaBitmap()
    val directory = File(System.getProperty("profile.screenshot.dir"))
    directory.mkdirs()
    Image.makeFromBitmap(bitmap).use { image ->
        image.encodeToData()!!.use { data -> File(directory, filename).writeBytes(data.bytes) }
    }
}
