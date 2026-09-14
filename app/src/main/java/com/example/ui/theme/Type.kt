package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val JameelNooriFamily = FontFamily(
    Font(R.font.jameel_noori_nastaleeq, FontWeight.Light),
    Font(R.font.jameel_noori_nastaleeq, FontWeight.Normal),
    Font(R.font.jameel_noori_nastaleeq, FontWeight.Medium),
    Font(R.font.jameel_noori_nastaleeq, FontWeight.SemiBold),
    Font(R.font.jameel_noori_nastaleeq, FontWeight.Bold),
    Font(R.font.jameel_noori_nastaleeq, FontWeight.ExtraBold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 50.sp),
    displayMedium = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 42.sp),
    displaySmall = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 32.sp),
    titleMedium = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 28.sp),
    titleSmall = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = JameelNooriFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 17.sp)
)

val Typography = AppTypography
