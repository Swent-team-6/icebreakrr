package com.github.se.icebreakrr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.resources.C
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.github.se.icebreakrr.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              Greeting("Android")
            }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  val ProfileTag = remember {
    mutableStateOf(
        listOf(
            Pair("salsa", Color.Red),
            Pair("pizza", Color.Red),
            Pair("cocaine", Color.Red),
            Pair("Maxime est un ptit gourmand", Color.Red),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
            Pair("Maxime est un ptit gourmand", Color.Yellow),
        ))
  }
  val stringQuery = remember { mutableStateOf("tennis") }
  val expanded = remember { mutableStateOf(true) }
  val tagsViewModel = TagsViewModel()
  TagSelector(ProfileTag, tagsViewModel, stringQuery, expanded, { Log.d("TEST", "CLICKED")})
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SampleAppTheme { Greeting("Android") }
}
