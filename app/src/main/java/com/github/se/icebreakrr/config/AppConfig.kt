package com.github.se.icebreakrr.config

import androidx.compose.runtime.staticCompositionLocalOf

// Define a CompositionLocal to hold the testing flag
val LocalIsTesting = staticCompositionLocalOf { false }
