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
import com.github.se.icebreakrr.ui.profile.ProfilePictureSelector
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Calendar

@Composable
fun ProfileCreationScreen(
    tagsViewModel: TagsViewModel,
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions
) {

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val profilePictureSize = screenWidth * 0.25f

  val scrollState = rememberScrollState()
  val fullName = remember { mutableStateOf("") }
  val catchphrase = remember { mutableStateOf("") }
  val description = remember { mutableStateOf("") }

  val tempBitmap = profilesViewModel.tempProfilePictureBitmap.collectAsState().value
  val user = profilesViewModel.selectedProfile.collectAsState().value

  val selectedTags = tagsViewModel.filteringTags.collectAsState().value
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = tagsViewModel.query.collectAsState()

  val expanded = remember { mutableStateOf(false) }

  var showDialog by remember { mutableStateOf(false) }
  var isModified by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val calendar =
      Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.YEAR, -20)
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
            val newCalendar =
                Calendar.getInstance().apply {
                  set(year, month, dayOfMonth)
                  set(Calendar.HOUR_OF_DAY, 0)
                  set(Calendar.MINUTE, 0)
                  set(Calendar.SECOND, 0)
                  set(Calendar.MILLISECOND, 0)
                }
            val pickedDate = newCalendar.time
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
  val inputTextStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .testTag("profileCreationContent"),
        horizontalAlignment = Alignment.CenterHorizontally) {

          // Profile Image
          ProfilePictureSelector(
              url = null,
              localBitmap = tempBitmap,
              size = profilePictureSize,
              onSelectionSuccess = { uri ->
                profilesViewModel.generateTempProfilePictureBitmap(context, uri)
                isModified = true
              },
              onSelectionFailure = {
                Toast.makeText(context, "Failed to select image", Toast.LENGTH_SHORT).show()
              })

          // Full Name Input
          OutlinedTextField(
              value = fullName.value,
              onValueChange = { fullName.value = it },
              label = { Text("Full Name", fontSize = 16.sp) },
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("fullName"),
              textStyle = inputTextStyle)

          // Birthdate Input
          OutlinedTextField(
              value = birthdate.value,
              onValueChange = { /* Read-only field */},
              label = { Text("Birthday", fontSize = 16.sp) },
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("birthdate"),
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
              label = { Text("Catchphrase", fontSize = 16.sp) },
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("catchphrase"),
              textStyle = inputTextStyle)

          // Description Input
          OutlinedTextField(
              value = description.value,
              onValueChange = { description.value = it },
              label = { Text("Description", fontSize = 16.sp) },
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("description"),
              textStyle = inputTextStyle,
              minLines = 3)

          Spacer(modifier = Modifier.height(16.dp))

          // Gender Selection Title
          Text(
              text = "Gender",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

          // Gender Selection Buttons Row
          Row(
              modifier =
                  Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("genderSelection"),
              horizontalArrangement = Arrangement.SpaceEvenly) {
                Gender.values().forEach { gender ->
                  OutlinedButton(
                      onClick = { selectedGender = gender },
                      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
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
              stringQuery = stringQuery.value,
              expanded = expanded,
              onTagClick = { tag ->
                tagsViewModel.removeFilter(tag)
                isModified = true
              },
              onStringChanged = {
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
                  val newProfile =
                      Profile(
                          uid = user.uid,
                          name = fullName.value,
                          gender = selectedGender!!, // Add this line
                          birthDate =
                              Timestamp(
                                  Calendar.getInstance()
                                      .apply {
                                        time = selectedDate.value!!
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                      }
                                      .time),
                          catchPhrase = catchphrase.value,
                          description = description.value,
                          tags = selectedTags,
                          fcmToken = fcmToken)
                  profilesViewModel.addNewProfile(newProfile)
                  // Upload profile picture if selected
                  profilesViewModel.validateAndUploadProfilePicture(context)
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
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).testTag("confirmButton")) {
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
