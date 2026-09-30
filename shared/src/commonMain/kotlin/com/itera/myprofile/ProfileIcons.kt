package com.itera.myprofile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Ikon vektor lokal: tetap tajam dan tidak memerlukan internet. */
object ProfileIcons {
    val Mail = outlineIcon("Email") {
        moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close()
        moveTo(3f, 6f); lineTo(12f, 13f); lineTo(21f, 6f)
    }
    val Phone = outlineIcon("Telepon") {
        moveTo(5f, 3f); lineTo(9f, 3f); lineTo(11f, 8f); lineTo(8f, 10f)
        curveTo(9f, 13f, 11f, 15f, 14f, 16f)
        lineTo(16f, 13f); lineTo(21f, 15f); lineTo(21f, 19f)
        curveTo(21f, 24f, 1f, 16f, 3f, 5f); close()
    }
    val Location = outlineIcon("Lokasi") {
        moveTo(20f, 10f); curveTo(20f, 16f, 12f, 22f, 12f, 22f)
        curveTo(12f, 22f, 4f, 16f, 4f, 10f)
        arcTo(8f, 8f, 0f, true, true, 20f, 10f)
        moveTo(15f, 10f); arcTo(3f, 3f, 0f, true, true, 9f, 10f)
        arcTo(3f, 3f, 0f, true, true, 15f, 10f)
    }
}

private fun outlineIcon(
    name: String,
    block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit,
): ImageVector = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.7f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
        pathBuilder = block,
    )
}.build()
