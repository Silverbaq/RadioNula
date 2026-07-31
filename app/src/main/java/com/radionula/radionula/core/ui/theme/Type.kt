package com.radionula.radionula.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.radionula.radionula.R

val Roboto = FontFamily(Font(R.font.roboto_regular))
val RobotoLight = FontFamily(Font(R.font.roboto_light))

/**
 * The XML only ever styled two things - a 16sp regular artist line and a 16sp
 * light title line - so the type scale stays that small on purpose.
 */
val NulaTypography = Typography(
    bodyLarge = TextStyle(fontFamily = Roboto, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = RobotoLight, fontSize = 16.sp),
)
