package com.github.se.icebreakrr.ui.sections

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.shared.UnsavedChangesDialog
import com.github.se.icebreakrr.ui.sections.shared.handleSafeBackNavigation
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore

// This file was written with the help of Cursor, Claude, ChatGPT

/** Constants used for layout calculations and default values */
const val textSizeFactor = 0.3f
const val textFieldHeightFactor = 0.08f
const val textFieldWidthFactor = 0.2f
const val buttonHeightFactor = 0.07f
const val buttonWidthFactor = 0.3f
const val tagSelectorHeightFactor = 0.25f
const val tagSelectorWidthFactor = 0.8f
const val tagSelectorTextSizeFactor = 0.1f
const val titleFontSizeFactor = 0.03f
const val MINIMUM_AGE = 13
const val EMPTY_FROM_INPUT_AGE = 0
const val GENDER_BUTTON_WIDTH_FACTOR = 0.3f
const val GENDER_BUTTON_HEIGHT_FACTOR = 0.06f
const val HORIZONTAL_SPACING_FACTOR = 0.02f
const val AGE_SEPARATOR_FONT_SIZE = 24
const val DEFAULT_PADDING = 16
const val SMALL_PADDING = 8
const val CORNER_RADIUS = 8
const val DEFAULT_RADIUS = 300
const val FILTER_ACTION_BUTTON_ALPHA = 0.5f
const val DEFAULT_USER_LATITUDE = 46.51827 // Lausanne
const val DEFAULT_USER_LONGITUDE = 6.619265 // Lausanne
val TEXTS_PADDING = 8.dp
val ALLOWED_VALUES = listOf(50, 100, 200, 300, 400, 500)

/**
 * Main composable for the Filter screen. Allows users to set and modify filtering criteria for
 * profiles, including gender preferences, age range, and tags.
 *
 * @param navigationActions Navigation handler for screen transitions
 * @param tagsViewModel ViewModel for managing tags
 * @param filterViewModel ViewModel for managing filter states
 * @param profilesViewModel ViewModel for managing profile data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel =
        viewModel(
            factory =
                TagsViewModel.Companion.Factory(
                    FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())),
    filterViewModel: FilterViewModel = viewModel(factory = FilterViewModel.Factory),
    profilesViewModel: ProfilesViewModel =
        viewModel(
            factory =
                ProfilesViewModel.Companion.Factory(FirebaseAuth.getInstance(), Firebase.firestore))
) {
  val context = LocalContext.current
  val currentFocusManager = LocalFocusManager.current

  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp.dp
  val screenWidth = configuration.screenWidthDp.dp
  val buttonWidth = screenWidth * buttonWidthFactor
  val buttonHeight = screenHeight * buttonHeightFactor
  val buttonTextSize = (buttonHeight.value * textSizeFactor).sp

  val selectedGenders by filterViewModel.selectedGenders.collectAsState()
  val radius by filterViewModel.selectedRadius.collectAsState()
  val ageRange = filterViewModel.ageRange.collectAsState()

  LaunchedEffect(Unit) {
    filterViewModel.filteredTags.value.forEach { tag -> tagsViewModel.addFilter(tag) }
  }

  // Store the new inputs
  val currentSelectedGenders = mutableListOf<Gender>()

  // Gender selection logic
  var manSelected by remember { mutableStateOf(selectedGenders.contains(Gender.MEN)) }
  var womanSelected by remember { mutableStateOf(selectedGenders.contains(Gender.WOMEN)) }
  var otherSelected by remember { mutableStateOf(selectedGenders.contains(Gender.OTHER)) }

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
  val stringQuery = remember { mutableStateOf("") }
  val savedFilteredTags = filterViewModel.filteredTags.collectAsState()
  val expanded = remember { mutableStateOf(false) }

  var ageRangeError by remember { mutableStateOf(false) }

  var showDialog by remember { mutableStateOf(false) }
  var isModified by remember { mutableStateOf(false) }

  var isAgeFromFocused by remember { mutableStateOf(false) }
  var isAgeToFocused by remember { mutableStateOf(false) }

  fun checkModified(): Boolean {
    // Check if gender selection changed
    val currentGenderSelection = mutableListOf<Gender>()
    if (manSelected) currentGenderSelection.add(Gender.MEN)
    if (womanSelected) currentGenderSelection.add(Gender.WOMEN)
    if (otherSelected) currentGenderSelection.add(Gender.OTHER)

    val gendersChanged = currentGenderSelection != filterViewModel.selectedGenders.value

    // Check if age range changed
    val currentAgeRange =
        when {
          ageFrom != null && ageTo != null && !ageRangeError -> ageFrom!!..ageTo!!
          ageFrom != null && ageTo == null -> ageFrom!!..Int.MAX_VALUE
          ageFrom == null && ageTo != null -> 0..ageTo!!
          else -> null
        }
    val ageRangeChanged = currentAgeRange != filterViewModel.ageRange.value

    // Check if tags changed
    val tagsChanged = tagsViewModel.filteringTags.value != filterViewModel.filteredTags.value

    return gendersChanged || ageRangeChanged || tagsChanged
  }

  fun validateAndUpdateAgeFrom(input: String) {
    ageFromInput = input
    if (input.isEmpty()) {
      ageFrom = null
      ageRangeError = false
    } else {
      val newAge = input.toIntOrNull()
      if (newAge != null && newAge >= MINIMUM_AGE) {
        ageFrom = newAge
        ageRangeError = ageTo != null && newAge > ageTo!!
      } else {
        ageFrom = null
        ageRangeError = false
      }
    }
    isModified = checkModified()
  }

  fun validateAndUpdateAgeTo(input: String) {
    ageToInput = input
    if (input.isEmpty()) {
      ageTo = null
      ageRangeError = false
    } else {
      val newAge = input.toIntOrNull()
      if (newAge != null && newAge >= MINIMUM_AGE) {
        ageTo = newAge
        ageRangeError = ageFrom != null && newAge < ageFrom!!
      } else {
        ageTo = null
        ageRangeError = false
      }
    }
    isModified = checkModified()
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

  BackHandler {
    handleSafeBackNavigation(
        isModified = isModified,
        setShowDialog = { showDialog = it },
        tagsViewModel = tagsViewModel,
        navigationActions = navigationActions,
        profilesViewModel = profilesViewModel)
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  stringResource(R.string.filter),
                  color = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.testTag("FilterTopBarTitle"))
            },
            modifier = Modifier.testTag("FilterTopBar"),
            navigationIcon = {
              IconButton(
                  onClick = {
                    handleSafeBackNavigation(
                        isModified = isModified,
                        setShowDialog = { showDialog = it },
                        tagsViewModel = tagsViewModel,
                        navigationActions = navigationActions,
                        profilesViewModel = profilesViewModel)
                  },
                  modifier = Modifier.testTag("Back Button")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary))
      }) { innerPadding ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Add scrolling functionality
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null) {
                          currentFocusManager.clearFocus()
                        }) {
              Column(
                  modifier =
                      Modifier.fillMaxSize().padding(innerPadding).padding(DEFAULT_PADDING.dp)) {
                    Text(
                        stringResource(R.string.gender),
                        fontSize = (screenHeight.value * titleFontSizeFactor).sp,
                        modifier =
                            Modifier.padding(vertical = TEXTS_PADDING).testTag("GenderTitle"))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                      GenderButton(
                          label = stringResource(R.string.gender_men),
                          selected = manSelected,
                          onClick = {
                            manSelected = !manSelected
                            isModified = checkModified()
                          },
                          buttonWidth = buttonWidth,
                          buttonHeight = buttonHeight)
                      GenderButton(
                          label = stringResource(R.string.gender_women),
                          selected = womanSelected,
                          onClick = {
                            womanSelected = !womanSelected
                            isModified = checkModified()
                          },
                          buttonWidth = buttonWidth,
                          buttonHeight = buttonHeight)
                      GenderButton(
                          label = stringResource(R.string.gender_other),
                          selected = otherSelected,
                          onClick = {
                            otherSelected = !otherSelected
                            isModified = checkModified()
                          },
                          buttonWidth = buttonWidth,
                          buttonHeight = buttonHeight)
                    }

                    Text(
                        stringResource(R.string.age_range),
                        fontSize = (screenHeight.value * titleFontSizeFactor).sp,
                        modifier =
                            Modifier.padding(vertical = TEXTS_PADDING).testTag("AgeRangeTitle"))

                    AgeRangeInputFields(
                        ageFromInput = ageFromInput,
                        ageToInput = ageToInput,
                        onAgeFromChange = { validateAndUpdateAgeFrom(it) },
                        onAgeToChange = { validateAndUpdateAgeTo(it) },
                        validateAgeRange = { validateAgeRange() },
                        ageFrom = ageFrom,
                        ageTo = ageTo,
                        ageRangeError = ageRangeError,
                        screenHeight = screenHeight,
                        screenWidth = screenWidth,
                        onFromFocusChanged = { isAgeFromFocused = it },
                        onToFocusChanged = { isAgeToFocused = it })

                    val tagSelectorHeight = screenHeight * tagSelectorHeightFactor
                    val tagSelectorWidth = screenWidth * tagSelectorWidthFactor

                    RadiusSlider(
                        radius = radius,
                        onDistanceChange = { newRadius ->
                          filterViewModel.setSelectedRadius(newRadius)
                        })
                    Text(
                        "Tags",
                        fontSize = (screenHeight.value * titleFontSizeFactor).sp,
                        modifier =
                            Modifier.padding(vertical = SMALL_PADDING.dp)
                                .testTag("TagsTitle")
                                .semantics(mergeDescendants = true) {})
                    TagSelector(
                        selectedTag =
                            filteringTags.value.map { tag ->
                              Pair(tag, tagsViewModel.tagToColor(tag))
                            },
                        outputTag =
                            tagsSuggestions.value.map { tag ->
                              Pair(tag, tagsViewModel.tagToColor(tag))
                            },
                        stringQuery = stringQuery,
                        expanded = expanded,
                        onTagClick = { tag ->
                          tagsViewModel.removeFilter(tag)
                          isModified = checkModified()
                        },
                        onStringChanged = {
                          stringQuery.value = it
                          tagsViewModel.setQuery(it, filteringTags.value)
                        },
                        textColor = MaterialTheme.colorScheme.onSurface,
                        onDropDownItemClicked = { tag ->
                          tagsViewModel.addFilter(tag)
                          isModified = checkModified()
                        },
                        height = tagSelectorHeight,
                        width = tagSelectorWidth,
                        textSize = (tagSelectorHeight.value * tagSelectorTextSizeFactor).sp)
                    FilterActionButton(
                        onClick = {
                          filterViewModel.setFilteredTags(tagsViewModel.filteringTags.value)
                          tagsViewModel.applyFilters()
                          tagsViewModel.leaveUI()

                          if (manSelected) currentSelectedGenders.add(Gender.MEN)
                          if (womanSelected) currentSelectedGenders.add(Gender.WOMEN)
                          if (otherSelected) currentSelectedGenders.add(Gender.OTHER)
                          filterViewModel.setGenders(currentSelectedGenders)

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
                              filterViewModel.setAgeRange(EMPTY_FROM_INPUT_AGE..ageTo!!)
                            }
                            else -> {
                              filterViewModel.setAgeRange(null)
                            }
                          }

                          profilesViewModel.getFilteredProfilesInRadius(
                              GeoPoint(DEFAULT_USER_LATITUDE, DEFAULT_USER_LONGITUDE),
                              filterViewModel.selectedRadius.value,
                              filterViewModel.selectedGenders.value,
                              filterViewModel.ageRange.value,
                              tagsViewModel.filteredTags.value)

                          navigationActions.goBack()
                        },
                        onReset = {
                          // Reset gender selection UI only
                          manSelected = false
                          womanSelected = false
                          otherSelected = false
                          currentSelectedGenders.clear()

                          // Reset age range UI only
                          ageFromInput = ""
                          ageToInput = ""
                          ageFrom = null
                          ageTo = null
                          ageRangeError = false

                          // Reset tags UI only
                          tagsViewModel.leaveUI()

                          // Set modified flag
                          isModified = checkModified()

                          // Show confirmation toast
                          Toast.makeText(
                                  context,
                                  "Page reset, click on Filter to save",
                                  Toast.LENGTH_SHORT)
                              .show()
                        },
                        buttonWidth = buttonWidth,
                        buttonHeight = buttonHeight,
                        buttonTextSize = buttonTextSize,
                        enabled = !isAgeFromFocused && !isAgeToFocused)
                  }

              // Dialog at the end of the Box
              if (showDialog) {
                UnsavedChangesDialog(
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    onConfirm = {
                      showDialog = false
                      tagsViewModel.leaveUI()
                      navigationActions.goBack()
                    })
              }
            }
      }
}

/**
 * Composable for a gender selection button with consistent styling.
 *
 * @param label Text to display on the button
 * @param selected Whether this gender is currently selected
 * @param onClick Callback for when the button is clicked
 * @param buttonWidth Width for the button
 * @param buttonHeight Height for the button
 */
@Composable
fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    buttonWidth: Dp,
    buttonHeight: Dp
) {
  Button(
      onClick = onClick,
      modifier =
          Modifier.width(buttonWidth).height(buttonHeight).testTag("GenderButton$label").semantics {
            this.selected = selected
          },
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (selected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant,
              contentColor =
                  if (selected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant),
      shape = RoundedCornerShape(CORNER_RADIUS.dp)) {
        Text(
            text = label,
            color =
                if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = (buttonHeight.value * textSizeFactor).sp)
      }
}

/**
 * Composable for the age range input fields. Provides two text fields for entering minimum and
 * maximum age values with validation.
 *
 * @param ageFromInput Current value of the minimum age field
 * @param ageToInput Current value of the maximum age field
 * @param onAgeFromChange Callback for minimum age changes
 * @param onAgeToChange Callback for maximum age changes
 * @param validateAgeRange Callback to validate the age range
 * @param ageFrom Parsed minimum age value
 * @param ageTo Parsed maximum age value
 * @param ageRangeError Whether there's an error in the age range
 * @param screenHeight Current screen height for responsive sizing
 * @param screenWidth Current screen width for responsive sizing
 * @param onFromFocusChanged Callback for minimum age field focus changes
 * @param onToFocusChanged Callback for maximum age field focus changes
 */
@Composable
fun AgeRangeInputFields(
    ageFromInput: String,
    ageToInput: String,
    onAgeFromChange: (String) -> Unit,
    onAgeToChange: (String) -> Unit,
    validateAgeRange: () -> Unit,
    ageFrom: Int?,
    ageTo: Int?,
    ageRangeError: Boolean,
    screenHeight: Dp,
    screenWidth: Dp,
    onFromFocusChanged: (Boolean) -> Unit,
    onToFocusChanged: (Boolean) -> Unit
) {
  val textFieldHeight = screenHeight * textFieldHeightFactor
  val textFieldWidth = screenWidth * textFieldWidthFactor

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = DEFAULT_PADDING.dp),
      horizontalArrangement = Arrangement.Center) {
        OutlinedTextField(
            value = ageFromInput,
            onValueChange = { newValue ->
              if (newValue.length <= 3) {
                onAgeFromChange(newValue.filter { it.isDigit() })
              }
            },
            keyboardOptions =
                KeyboardOptions(
                    autoCorrectEnabled = false, keyboardType = KeyboardType.NumberPassword),
            maxLines = 1,
            modifier =
                Modifier.height(IntrinsicSize.Min)
                    .width(textFieldWidth)
                    .testTag("AgeFromTextField")
                    .onFocusChanged {
                      if (!it.isFocused) validateAgeRange()
                      onFromFocusChanged(it.isFocused)
                    },
            label = { Text("From") },
            singleLine = true,
            textStyle = TextStyle(fontSize = (textFieldHeight.value * textSizeFactor).sp),
            isError = ageFromInput.isNotEmpty() && (ageFrom == null || ageRangeError))
        Spacer(modifier = Modifier.width(screenWidth * HORIZONTAL_SPACING_FACTOR))
        Text(
            " - ",
            modifier =
                Modifier.padding(horizontal = SMALL_PADDING.dp).align(Alignment.CenterVertically),
            fontSize = AGE_SEPARATOR_FONT_SIZE.sp,
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(screenWidth * HORIZONTAL_SPACING_FACTOR))
        OutlinedTextField(
            value = ageToInput,
            onValueChange = { newValue ->
              if (newValue.length <= 3) {
                onAgeToChange(newValue.filter { it.isDigit() })
              }
            },
            keyboardOptions =
                KeyboardOptions(
                    autoCorrectEnabled = false, keyboardType = KeyboardType.NumberPassword),
            maxLines = 1,
            modifier =
                Modifier.height(IntrinsicSize.Min)
                    .width(textFieldWidth)
                    .testTag("AgeToTextField")
                    .onFocusChanged {
                      if (!it.isFocused) validateAgeRange()
                      onToFocusChanged(it.isFocused)
                    },
            label = { Text("To") },
            singleLine = true,
            textStyle = TextStyle(fontSize = (textFieldHeight.value * textSizeFactor).sp),
            isError = ageToInput.isNotEmpty() && (ageTo == null || ageRangeError))
      }
}

/**
 * A composable function that displays a slider to adjust the search radius dynamically.
 *
 * @param radius The current radius value in meters.
 * @param onDistanceChange Callback invoked when the slider value changes, providing the new radius.
 */
@Composable
fun RadiusSlider(radius: Int, onDistanceChange: (Int) -> Unit) {
  val currentIndex = ALLOWED_VALUES.indexOf(radius)
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp

  Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = DEFAULT_PADDING.dp),
  ) {
    Text(
        "Search Radius: $radius m",
        fontSize = (screenHeight.value * titleFontSizeFactor).sp,
        modifier = Modifier.padding(vertical = TEXTS_PADDING).testTag("RadiusSliderTitle"))

    Slider(
        value = currentIndex.toFloat(),
        onValueChange = { newValue ->
          val newIndex = newValue.toInt()
          onDistanceChange(ALLOWED_VALUES[newIndex])
        },
        valueRange = 0f..(ALLOWED_VALUES.size - 1).toFloat(),
        steps = ALLOWED_VALUES.size - 2,
        modifier = Modifier.padding(horizontal = (2 * DEFAULT_PADDING).dp).testTag("RadiusSlider"),
        colors =
            SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary))
  }
}

/**
 * Composable for the filter action buttons (Filter and Reset). The Filter button is disabled when
 * text fields are focused to prevent accidental submissions while typing.
 *
 * @param onClick Callback for when the Filter button is clicked
 * @param onReset Callback for when the Reset button is clicked
 * @param buttonWidth Width for both buttons
 * @param buttonHeight Height for both buttons
 * @param buttonTextSize Text size for button labels
 * @param enabled Whether the Filter button is enabled
 */
@Composable
fun FilterActionButton(
    onClick: () -> Unit,
    onReset: () -> Unit,
    buttonWidth: Dp,
    buttonHeight: Dp,
    buttonTextSize: TextUnit,
    enabled: Boolean = true
) {
  Box(modifier = Modifier.fillMaxWidth().padding(DEFAULT_PADDING.dp)) {
    Row(
        modifier = Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING.dp)) {
          // Reset Button
          Button(
              onClick = onReset,
              modifier = Modifier.width(buttonWidth).height(buttonHeight).testTag("ResetButton"),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant,
                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Text(
                    "Reset",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = buttonTextSize)
              }

          // Filter Button
          Button(
              onClick = onClick,
              enabled = enabled,
              modifier = Modifier.width(buttonWidth).height(buttonHeight).testTag("FilterButton"),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary,
                      disabledContainerColor =
                          MaterialTheme.colorScheme.primary.copy(
                              alpha = FILTER_ACTION_BUTTON_ALPHA))) {
                Text(
                    "Filter",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = buttonTextSize)
              }
        }
  }
}
