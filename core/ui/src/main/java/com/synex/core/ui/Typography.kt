package com.synex.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val SynexTypography = Typography(
    displaySmall = synexText(42, 44, FontWeight.Medium, -1.4),
    headlineLarge = synexText(32, 35, FontWeight.Medium, -1.0),
    headlineMedium = synexText(24, 28, FontWeight.Medium, -0.5),
    titleLarge = synexText(18, 22, FontWeight.SemiBold),
    titleMedium = synexText(15, 19, FontWeight.SemiBold),
    bodyLarge = synexText(16, 23, FontWeight.Normal),
    bodyMedium = synexText(14, 20, FontWeight.Normal),
    labelLarge = synexText(13, 16, FontWeight.SemiBold, 0.2),
    labelMedium = synexText(12, 15, FontWeight.Medium, 0.4),
)

private fun synexText(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    spacing: Double = 0.0,
) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = spacing.sp,
)
