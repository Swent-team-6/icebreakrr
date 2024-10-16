package com.github.se.icebreakrr.ui.sections

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.tags.TagSelector

const val textSizeFactor = 0.3f
val IcebreakrrBlue: Color = Color(0xFF1FAEF0)

// This file was written with the help of Cursor, Claude, ChatGPT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(navigationActions: NavigationActions) {
  val context = LocalContext.current

  val textFieldHeightFactor = 0.08f
  val textFieldWidthFactor = 0.2f
  val buttonHeightFactor = 0.07f
  val buttonWidthFactor = 0.3f
  val tagSelectorHeightFactor = 0.25f
  val tagSelectorWidthFactor = 0.8f
  val tagSelectorTextSizeFactor = 0.1f
  val titleFontSizeFactor = 0.03f

  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp.dp
  val screenWidth = configuration.screenWidthDp.dp
  val buttonWidth = screenWidth * buttonWidthFactor
  val buttonHeight = screenHeight * buttonHeightFactor
  val buttonTextSize = (buttonHeight.value * textSizeFactor).sp

  var manSelected by remember { mutableStateOf(false) }
  var womanSelected by remember { mutableStateOf(false) }
  var otherSelected by remember { mutableStateOf(false) }
  var ageFrom by remember { mutableStateOf<Int?>(null) }
  var ageTo by remember { mutableStateOf<Int?>(null) }

  // test values
  var selectedTags = remember {
    mutableStateOf<List<Pair<String, Color>>>(listOf(Pair("salsa", Color.Red)))
  }
  val availableTags = remember {
    mutableStateOf<List<Pair<String, Color>>>(
        listOf(
            Pair("salsa", Color.Red),
            Pair("pesto", Color.Green),
            Pair("psto", Color.Green),
            Pair("pest", Color.Green),
            Pair("peest", Color.Green)))
  }

  val stringQuery = remember { mutableStateOf("") }
  val expanded = remember { mutableStateOf(false) }

  var ageFromText by remember { mutableStateOf("") }
  var ageToText by remember { mutableStateOf("") }
  var ageRangeError by remember { mutableStateOf(false) }

  fun validateAndUpdateAgeFrom(input: String) {
    ageFromText = input
    if (input.isEmpty()) {
      ageFrom = null
      ageRangeError = false
    } else {
      val newAge = input.toIntOrNull()
      if (newAge != null && newAge >= 13) {
        ageFrom = newAge
        ageRangeError = ageTo != null && newAge > ageTo!!
      } else {
        ageFrom = null
        ageRangeError = false
      }
    }
  }

  fun validateAndUpdateAgeTo(input: String) {
    ageToText = input
    if (input.isEmpty()) {
      ageTo = null
      ageRangeError = false
    } else {
      val newAge = input.toIntOrNull()
      if (newAge != null && newAge >= 13) {
        ageTo = newAge
        ageRangeError = ageFrom != null && newAge < ageFrom!!
      } else {
        ageTo = null
        ageRangeError = false
      }
    }
  }

  fun validateAgeRange() {
    when {
      ageRangeError -> {
        Toast.makeText(context, "Invalid age range", Toast.LENGTH_SHORT).show()
        ageToText = ""
        ageFromText = ""
        return // exit early so that there aren't 3 overlapping toasts as the next 2 would also be
        // called
      }
      ageFromText.isNotEmpty() && ageFrom == null -> {
        Toast.makeText(context, "Please enter a valid 'From' age (13 or older)", Toast.LENGTH_SHORT)
            .show()
        ageFromText = "" // Clear invalid input
      }
      ageToText.isNotEmpty() && ageTo == null -> {
        Toast.makeText(context, "Please enter a valid 'To' age (13 or older)", Toast.LENGTH_SHORT)
            .show()
        ageToText = "" // Clear invalid input
      }
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text("Filter", color = Color.White, modifier = Modifier.testTag("FilterTopBarTitle"))
            },
            modifier = Modifier.testTag("FilterTopBar"),
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("Back Button")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = IcebreakrrBlue))
      }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
          Text(
              "Gender",
              fontSize = (screenHeight.value * titleFontSizeFactor).sp,
              modifier = Modifier.padding(vertical = 8.dp).testTag("GenderTitle"))

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            GenderButton(
                selected = manSelected, onClick = { manSelected = !manSelected }, label = "Men")
            GenderButton(
                selected = womanSelected,
                onClick = { womanSelected = !womanSelected },
                label = "Women")
            GenderButton(
                selected = otherSelected,
                onClick = { otherSelected = !otherSelected },
                label = "Other")
          }

          Text(
              "Age Range",
              fontSize = (screenHeight.value * titleFontSizeFactor).sp,
              modifier = Modifier.padding(vertical = 8.dp).testTag("AgeRangeTitle"))
          val textFieldHeight = screenHeight * textFieldHeightFactor
          val textFieldWidth = screenWidth * textFieldWidthFactor

          Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.Center) {
                OutlinedTextField(
                    value = ageFromText,
                    onValueChange = { validateAndUpdateAgeFrom(it) },
                    modifier =
                        Modifier.height(IntrinsicSize.Min)
                            .width(textFieldWidth)
                            .testTag("AgeFromTextField")
                            .onFocusChanged { if (!it.isFocused) validateAgeRange() },
                    label = { Text("From") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = (textFieldHeight.value * textSizeFactor).sp),
                    isError = ageFromText.isNotEmpty() && (ageFrom == null || ageRangeError))
                Spacer(modifier = Modifier.width(screenWidth * 0.02f))
                Text(
                    " - ",
                    modifier =
                        Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(screenWidth * 0.02f))
                OutlinedTextField(
                    value = ageToText,
                    onValueChange = { validateAndUpdateAgeTo(it) },
                    modifier =
                        Modifier.height(IntrinsicSize.Min)
                            .width(textFieldWidth)
                            .testTag("AgeToTextField")
                            .onFocusChanged { if (!it.isFocused) validateAgeRange() },
                    label = { Text("To") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = (textFieldHeight.value * textSizeFactor).sp),
                    isError = ageToText.isNotEmpty() && (ageTo == null || ageRangeError))
              }

          val tagSelectorHeight = screenHeight * tagSelectorHeightFactor
          val tagSelectorWidth = screenWidth * tagSelectorWidthFactor

          Text(
              "Tags",
              fontSize = (screenHeight.value * titleFontSizeFactor).sp,
              modifier = Modifier.padding(vertical = 8.dp).testTag("TagsTitle"))
          TagSelector(
              selectedTag = selectedTags,
              outputTag = availableTags,
              stringQuery = stringQuery,
              expanded = expanded,
              onTagClick = { t ->
                if (selectedTags.value.any { it.first == t }) {
                  selectedTags.value = selectedTags.value.filter { it.first != t }
                }
              },
              onStringChanged = {},
              textColor = MaterialTheme.colorScheme.onSurface,
              onDropDownItemClicked = { tag ->
                if (selectedTags.value.none { it.first == tag }) {
                  availableTags.value
                      .firstOrNull { it.first == tag }
                      ?.let { newTag -> selectedTags.value += newTag }
                }
              },
              height = tagSelectorHeight,
              width = tagSelectorWidth,
              textSize = (tagSelectorHeight.value * tagSelectorTextSizeFactor).sp)
          Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = { navigationActions.navigateTo(Screen.AROUND_YOU) },
                modifier =
                    Modifier.width(buttonWidth)
                        .height(buttonHeight)
                        .testTag("FilterButton")
                        .align(Alignment.Center),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = IcebreakrrBlue, contentColor = Color.White)) {
                  Text("Filter", color = Color.White, fontSize = buttonTextSize)
                }
          }
        }
      }
}

@Composable
fun GenderButton(selected: Boolean, onClick: () -> Unit, label: String) {
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp

  val genderButtonWidthFactor = 0.3f
  val genderButtonHeightFactor = 0.06f

  val buttonWidth = screenWidth * genderButtonWidthFactor
  val buttonHeight = screenHeight * genderButtonHeightFactor

  Button(
      onClick = onClick,
      modifier =
          Modifier.width(buttonWidth).height(buttonHeight).testTag("GenderButton$label").semantics {
            this.selected = selected
          },
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (selected) IcebreakrrBlue else MaterialTheme.colorScheme.surfaceVariant),
      shape = RoundedCornerShape(8.dp)) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = (buttonHeight.value * textSizeFactor).sp,
        )
      }
}
