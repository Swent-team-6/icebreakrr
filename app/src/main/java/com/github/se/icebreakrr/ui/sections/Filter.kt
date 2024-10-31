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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.TagSelector

const val textSizeFactor = 0.3f
val IcebreakrrBlue: Color = Color(0xFF1FAEF0)
const val textFieldHeightFactor = 0.08f
const val textFieldWidthFactor = 0.2f
const val buttonHeightFactor = 0.07f
const val buttonWidthFactor = 0.3f
const val tagSelectorHeightFactor = 0.25f
const val tagSelectorWidthFactor = 0.8f
const val tagSelectorTextSizeFactor = 0.1f
const val titleFontSizeFactor = 0.03f

// This file was written with the help of Cursor, Claude, ChatGPT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Factory),
    filterViewModel: FilterViewModel = viewModel(factory = FilterViewModel.Factory)
) {
  val context = LocalContext.current

  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp.dp
  val screenWidth = configuration.screenWidthDp.dp
  val buttonWidth = screenWidth * buttonWidthFactor
  val buttonHeight = screenHeight * buttonHeightFactor
  val buttonTextSize = (buttonHeight.value * textSizeFactor).sp

  // Read the selected genders from the ViewModel
  val selectedGenders by filterViewModel.selectedGenders.collectAsState()

  // Gender selection logic
  var manSelected by remember { mutableStateOf(selectedGenders.contains(Gender.MEN)) }
  var womanSelected by remember { mutableStateOf(selectedGenders.contains(Gender.WOMEN)) }
  var otherSelected by remember { mutableStateOf(selectedGenders.contains(Gender.OTHER)) }

  val ageRange = filterViewModel.ageRange.collectAsState()

  var ageFromInput by remember { mutableStateOf("") }
  var ageFrom by remember { mutableStateOf<Int?>(null) }
  if (ageRange.value != null && (ageRange.value?.start) != 0) {
    ageFromInput = ageRange.value?.start.toString()
    ageFrom = ageRange.value?.start
  }

  var ageToInput by remember { mutableStateOf("") }
  var ageTo by remember { mutableStateOf<Int?>(null) }
  if (ageRange.value != null && (ageRange.value?.endInclusive) != Int.MAX_VALUE) {
    ageTo = ageRange.value?.endInclusive
    ageToInput = ageRange.value?.endInclusive.toString()
  }

  val filteringTags = tagsViewModel.filteringTags.collectAsState()
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = tagsViewModel.query.collectAsState()
  val savedFilteredTags = filterViewModel.filteredTags.collectAsState()
  savedFilteredTags.value.forEach { tag -> tagsViewModel.addFilter(tag) }
  val expanded = remember { mutableStateOf(false) }

  var ageRangeError by remember { mutableStateOf(false) }

  fun validateAndUpdateAgeFrom(input: String) {
    ageFromInput = input
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
    ageToInput = input
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
        ageToInput = ""
        ageFromInput = ""
        return // exit early so that there aren't 3 overlapping toasts as the next 2 would also be
        // called
      }
      ageFromInput.isNotEmpty() && ageFrom == null -> {
        Toast.makeText(context, "Please enter a valid 'From' age (13 or older)", Toast.LENGTH_SHORT)
            .show()
        ageFromInput = "" // Clear invalid input
      }
      ageToInput.isNotEmpty() && ageTo == null -> {
        Toast.makeText(context, "Please enter a valid 'To' age (13 or older)", Toast.LENGTH_SHORT)
            .show()
        ageToInput = "" // Clear invalid input
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
                  onClick = {
                    tagsViewModel.leaveUI()
                    navigationActions.goBack()
                  },
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
                    value = ageFromInput,
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
                    isError = ageFromInput.isNotEmpty() && (ageFrom == null || ageRangeError))
                Spacer(modifier = Modifier.width(screenWidth * 0.02f))
                Text(
                    " - ",
                    modifier =
                        Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(screenWidth * 0.02f))
                OutlinedTextField(
                    value = ageToInput,
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
                    isError = ageToInput.isNotEmpty() && (ageTo == null || ageRangeError))
              }

          val tagSelectorHeight = screenHeight * tagSelectorHeightFactor
          val tagSelectorWidth = screenWidth * tagSelectorWidthFactor

          Text(
              "Tags",
              fontSize = (screenHeight.value * titleFontSizeFactor).sp,
              modifier = Modifier.padding(vertical = 8.dp).testTag("TagsTitle"))
          TagSelector(
              selectedTag =
                  filteringTags.value.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
              outputTag =
                  tagsSuggestions.value.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
              stringQuery = stringQuery.value,
              expanded = expanded,
              onTagClick = { tag -> tagsViewModel.removeFilter(tag) },
              onStringChanged = { tagsViewModel.setQuery(it, filteringTags.value) },
              textColor = MaterialTheme.colorScheme.onSurface,
              onDropDownItemClicked = { tag -> tagsViewModel.addFilter(tag) },
              height = tagSelectorHeight,
              width = tagSelectorWidth,
              textSize = (tagSelectorHeight.value * tagSelectorTextSizeFactor).sp)
          Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = {
                  filterViewModel.setFilteredTags(tagsViewModel.filteringTags.value)
                  tagsViewModel.applyFilters()
                  tagsViewModel.leaveUI()

                  val selectedGenders = mutableListOf<Gender>()
                  if (manSelected) selectedGenders.add(Gender.MEN)
                  if (womanSelected) selectedGenders.add(Gender.WOMEN)
                  if (otherSelected) selectedGenders.add(Gender.OTHER)
                  filterViewModel.setGenders(selectedGenders)

                  when {
                    ageFrom != null && ageTo != null && !ageRangeError -> {
                      filterViewModel.setAgeRange(ageFrom!!..ageTo!!)
                    }
                    ageFrom == null && ageTo == null -> {
                      filterViewModel.setAgeRange(null)
                    }
                    ageFrom != null && ageTo == null -> {
                      filterViewModel.setAgeRange(ageFrom!!..Int.MAX_VALUE)
                    }
                    ageFrom == null && ageTo != null -> {
                      filterViewModel.setAgeRange(0..ageTo!!)
                    }
                    else -> {
                      filterViewModel.setAgeRange(null)
                    }
                  }

                  navigationActions.goBack()
                },
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
