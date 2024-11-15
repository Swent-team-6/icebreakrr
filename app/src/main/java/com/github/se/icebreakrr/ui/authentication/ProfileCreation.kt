import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Calendar

// Add these constants at the top of the file, before the ProfileCreationScreen composable
private object ProfileCreationConstants {
  // Layout constants
  const val DEFAULT_PADDING = 16
  const val VERTICAL_SPACING = 8
  const val SECTION_SPACING = 16

  // Size multipliers
  const val PROFILE_PICTURE_WIDTH_MULTIPLIER = 0.25f
  const val TEXT_SIZE_MULTIPLIER = 0.08f

  // Font sizes
  const val INPUT_LABEL_FONT_SIZE = 16
  const val INPUT_TEXT_FONT_SIZE = 18

  // Text field properties
  const val DESCRIPTION_MIN_LINES = 3

  // Date constants
  const val DEFAULT_AGE_YEARS = -20
}

/**
 * Displays the page of profile creation
 *
 * @param tagsViewModel : the view model of the tags
 * @param profilesViewModel : the view model of the profiles
 * @param navigationActions : the navigation action to navigate between screens
 */
@Composable
fun ProfileCreationScreen(
    tagsViewModel: TagsViewModel,
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions
) {

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val profilePictureSize = screenWidth * ProfileCreationConstants.PROFILE_PICTURE_WIDTH_MULTIPLIER
  val textSize = screenWidth * ProfileCreationConstants.TEXT_SIZE_MULTIPLIER

  val scrollState = rememberScrollState()
  val fullName = remember { mutableStateOf("") }
  val catchphrase = remember { mutableStateOf("") }
  val description = remember { mutableStateOf("") }

  val selectedTags = tagsViewModel.filteringTags.collectAsState().value
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = remember { mutableStateOf("") }

  val expanded = remember { mutableStateOf(false) }

  var isModified by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val calendar =
      Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.YEAR, ProfileCreationConstants.DEFAULT_AGE_YEARS)
      }
  val defaultDate = calendar.time

  val birthdate = remember { mutableStateOf(DateUtils.formatDate(defaultDate)) }
  val selectedDate = remember { mutableStateOf(defaultDate) }
  var isDateValid = remember { mutableStateOf(true) }

  var selectedGender by remember { mutableStateOf<Gender?>(null) }

  // Add form validation
  fun isFormValid(): Boolean {
    return fullName.value.isNotBlank() &&
        selectedDate.value != null &&
        isDateValid.value &&
        catchphrase.value.isNotBlank() &&
        description.value.isNotBlank() &&
        selectedGender != null
  }

  // Create DatePicker
  val datePicker =
      DatePickerDialog(
          context,
          { _, year, month, dayOfMonth ->
            calendar.apply { set(year, month, dayOfMonth) }
            val pickedDate = calendar.time
            selectedDate.value = pickedDate
            birthdate.value = DateUtils.formatDate(pickedDate)
            isDateValid.value = DateUtils.isAgeValid(pickedDate)
          },
          calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH),
          calendar.get(Calendar.DAY_OF_MONTH))

  // Set the maximum date to today
  datePicker.datePicker.maxDate = System.currentTimeMillis()

  // Add this TextStyle for consistent sizing
  val inputTextStyle =
      TextStyle(
          fontSize = ProfileCreationConstants.INPUT_TEXT_FONT_SIZE.sp,
          color = MaterialTheme.colorScheme.onSurface)

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(ProfileCreationConstants.DEFAULT_PADDING.dp)
                .verticalScroll(scrollState)
                .testTag("profileCreationContent"),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = "Create Profile",
              style = MaterialTheme.typography.headlineMedium,
              modifier = Modifier.padding(bottom = 16.dp).testTag("profileCreationTitle"))

          // Full Name Input
          OutlinedTextField(
              value = fullName.value,
              onValueChange = { fullName.value = it },
              label = {
                Text("Full Name", fontSize = ProfileCreationConstants.INPUT_LABEL_FONT_SIZE.sp)
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = ProfileCreationConstants.VERTICAL_SPACING.dp)
                      .testTag("fullName"),
              textStyle = inputTextStyle)

          // Birthdate Input
          OutlinedTextField(
              value = birthdate.value,
              onValueChange = { /* Read-only field */},
              label = {
                Text("Birthday", fontSize = ProfileCreationConstants.INPUT_LABEL_FONT_SIZE.sp)
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = ProfileCreationConstants.VERTICAL_SPACING.dp)
                      .testTag("birthdate"),
              textStyle = inputTextStyle,
              readOnly = true,
              trailingIcon = {
                IconButton(onClick = { datePicker.show() }) {
                  Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select date")
                }
              },
              isError = !isDateValid.value,
              supportingText = {
                if (!isDateValid.value) {
                  Text(
                      text = "You must be at least 13 years old",
                      color = MaterialTheme.colorScheme.error,
                      fontSize = 14.sp)
                }
              })

          // Catchphrase Input
          OutlinedTextField(
              value = catchphrase.value,
              onValueChange = { catchphrase.value = it },
              label = {
                Text("Catchphrase", fontSize = ProfileCreationConstants.INPUT_LABEL_FONT_SIZE.sp)
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = ProfileCreationConstants.VERTICAL_SPACING.dp)
                      .testTag("catchphrase"),
              textStyle = inputTextStyle)

          // Description Input
          OutlinedTextField(
              value = description.value,
              onValueChange = { description.value = it },
              label = {
                Text("Description", fontSize = ProfileCreationConstants.INPUT_LABEL_FONT_SIZE.sp)
              },
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = ProfileCreationConstants.VERTICAL_SPACING.dp)
                      .testTag("description"),
              textStyle = inputTextStyle,
              minLines = ProfileCreationConstants.DESCRIPTION_MIN_LINES)

          Spacer(modifier = Modifier.height(ProfileCreationConstants.SECTION_SPACING.dp))

          // Gender Selection Title
          Text(
              text = "Gender",
              style = MaterialTheme.typography.titleMedium,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(bottom = ProfileCreationConstants.VERTICAL_SPACING.dp))

          // Gender Selection Buttons Row
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = ProfileCreationConstants.VERTICAL_SPACING.dp)
                      .testTag("genderSelection"),
              horizontalArrangement = Arrangement.SpaceEvenly) {
                Gender.values().forEach { gender ->
                  OutlinedButton(
                      onClick = { selectedGender = gender },
                      modifier =
                          Modifier.weight(1f)
                              .padding(horizontal = ProfileCreationConstants.VERTICAL_SPACING.dp),
                      colors =
                          ButtonDefaults.outlinedButtonColors(
                              containerColor =
                                  if (selectedGender == gender)
                                      MaterialTheme.colorScheme.primaryContainer
                                  else MaterialTheme.colorScheme.surface)) {
                        Text(
                            text = gender.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                if (selectedGender == gender)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface)
                      }
                }
              }

          TagSelector(
              selectedTag = selectedTags.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
              outputTag =
                  tagsSuggestions.value.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
              stringQuery = stringQuery,
              expanded = expanded,
              onTagClick = { tag ->
                tagsViewModel.removeFilter(tag)
                isModified = true
              },
              onStringChanged = {
                stringQuery.value = it
                tagsViewModel.setQuery(it, selectedTags)
                isModified = true
              },
              textColor = MaterialTheme.colorScheme.onSurface,
              onDropDownItemClicked = { tag ->
                tagsViewModel.addFilter(tag)
                isModified = true
              },
              height = screenHeight,
              width = screenWidth,
              textSize = (screenHeight.value * 0.03).sp)
        }

    // Add confirmation button in top right corner
    IconButton(
        onClick = {
          if (isFormValid()) {
            val currentUser = Firebase.auth.currentUser
            currentUser?.let { user ->
              FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                  val fcmToken = task.result
                  calendar.time = selectedDate.value!!
                  val newProfile =
                      Profile(
                          uid = user.uid,
                          name = fullName.value,
                          gender = selectedGender!!, // Add this line
                          birthDate = Timestamp(calendar.time),
                          catchPhrase = catchphrase.value,
                          description = description.value,
                          tags = selectedTags,
                          fcmToken = fcmToken)
                  // Creates profile in DB
                  profilesViewModel.addNewProfile(newProfile)
                  // Navigate to AroundYou screen
                  navigationActions.navigateTo(TopLevelDestinations.AROUND_YOU)
                }
              }
            }
          } else {
            Toast.makeText(context, "Please fill all required fields correctly", Toast.LENGTH_SHORT)
                .show()
          }
        },
        modifier =
            Modifier.align(Alignment.TopEnd)
                .padding(ProfileCreationConstants.DEFAULT_PADDING.dp)
                .testTag("confirmButton")) {
          Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Confirm",
              tint = MaterialTheme.colorScheme.primary)
        }
  }
}

@Composable
fun Chip(tag: String) {
  Surface(
      shape = MaterialTheme.shapes.medium,
      color = Color.LightGray,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
        Text(tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
      }
}
