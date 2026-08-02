package com.radionula.radionula.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.radionula.radionula.resources.Res
import com.radionula.radionula.resources.roboto_light
import com.radionula.radionula.resources.roboto_regular
import org.jetbrains.compose.resources.Font

/**
 * The XML only ever styled two things - a 16sp regular artist line and a 16sp
 * light title line - so the type scale stays that small on purpose.
 *
 * A function rather than a val: Compose Multiplatform loads fonts through a
 * @Composable Font(), so the families cannot be built at class-init time.
 */
@Composable
fun nulaTypography(): Typography {
    val roboto = FontFamily(Font(Res.font.roboto_regular))
    val robotoLight = FontFamily(Font(Res.font.roboto_light))
    return Typography(
        bodyLarge = TextStyle(fontFamily = roboto, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = robotoLight, fontSize = 16.sp),
    )
}
