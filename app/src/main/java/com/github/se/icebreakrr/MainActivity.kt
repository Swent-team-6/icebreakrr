package com.github.se.icebreakrr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.resources.C
import com.github.se.icebreakrr.ui.theme.SampleAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
      val firebaseApp = FirebaseApp.initializeApp(this)
      Log.e("TagsR", "firebaseApp : $firebaseApp")
    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              ToBeCalled()
            }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier.semantics { testTag = C.Tag.greeting })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SampleAppTheme { Greeting("Android") }
}
@Composable
fun ToBeCalled(){
    Button(onClick = { addCategory() }) { }
}

fun addCategory(){
    //FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    Log.e("TagsR", "debug")
    try{
        val repo = TagsRepository(FirebaseFirestore.getInstance())
    }catch(e: Exception){
        Log.e("TagsR", "error : $e")
    }
    val repo = TagsRepository(FirebaseFirestore.getInstance())
    Log.e("TagsR", "debugb")
    repo.addCategory({}, "Sport",
        listOf("Football",
            "Basketball",
            "Sport",
            "Tennis",
            "PingPong",
            "Running",
            "Trail",
            "Cycling",
            "Racing",
            "Climbing",
            "Fitness",
            "Yoga",
            "Rugby",
            "Cricket",
            "Volleyball",
            "Hockey",
            "Handball",
            "WaterPolo",
            "Ultimate",
            "Badminton",
            "Squash",
            "Boxing",
            "Judo",
            "Karate",
            "Taekwondo",
            "MMA",
            "Kickboxing",
            "Swimming",
            "Surf",
            "Sailing",
            "Diving",
            "Kayaking",
            "Canoeing",
            "Windsurfing",
            "Athletism",
            "Ski",
            "Snowboard",
            "Karting",
            "Skateboarding",
            "Parkour",
            "Golf",
            "Bowling",
            "HorseRacing",
            "Polo",
            "CrossFit",
            "Camping",
            "Hiking",
            "Fitness"
        ), Color.Green.toString()
    )
}