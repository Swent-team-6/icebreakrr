package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.icebreakrr.ui.navigation.NavigationActions

/**
 * Composable function for displaying the "Around You" screen.
 *
 * It includes a bottom navigation bar and displays the main content of the screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FilterScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.testTag("filterScreen"),
      content = { innerPadding ->
        // Use the innerPadding to apply padding around your content
        Text(
            text = "Filter screen",
            modifier = Modifier.padding(innerPadding) // Applying the padding to the content
            )
      })
}
