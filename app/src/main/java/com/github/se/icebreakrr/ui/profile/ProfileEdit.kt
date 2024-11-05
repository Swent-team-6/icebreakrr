package com.github.se.icebreakrr.ui.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.tagSelectorTextSizeFactor
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditingScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Factory),
    profilesViewModel: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Factory)
) {

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  // Define dynamic padding and text size based on screen size
  val padding = screenWidth * 0.02f // 2% of screen width
  val textSize = screenWidth * 0.08f // 6% of screen width

  // Define dynamic sizes for other UI elements
  val profilePictureSize = screenWidth * 0.25f // 25% of screen width
  val catchphraseHeight = screenHeight * 0.12f // 12% of screen height
  val descriptionHeight = screenHeight * 0.20f // 20% of screen height

  // TODO : make the padding and text size dynamic based on screen size

  var user by remember {
    mutableStateOf(
        Profile(
            uid = "",
            name = "",
            gender = Gender.OTHER,
            birthDate = Timestamp.now(),
            catchPhrase = "",
            description = "",
            tags = listOf(),
            profilePictureUrl = ""))
  }

  var profilePicture by remember { mutableStateOf(user.profilePictureUrl) }
  var catchphrase by remember { mutableStateOf(TextFieldValue(user.catchPhrase)) }
  var description by remember { mutableStateOf(TextFieldValue(user.description)) }
  var tags by remember { mutableStateOf(user.tags) }

  LaunchedEffect(Unit) {
    withTimeoutOrNull(10000) {
      profilesViewModel.getProfileByUid(Firebase.auth.currentUser!!.uid)
      profilesViewModel.loading.first { !it }
    } ?: Log.w("ProfileEdit", "Error while loading user profile")
    user = profilesViewModel.selectedProfile.value?.copy() ?: user
    catchphrase = TextFieldValue(user.catchPhrase)
    description = TextFieldValue(user.description)
    profilePicture = user.profilePictureUrl
    Log.i("ProfileEdit", "User loaded: ${user.tags}")
    tags = user.tags
  }

  var showDialogBack by remember { mutableStateOf(false) }
  var showDialogConfirm by remember { mutableStateOf(false) }

  fun updateProfile() {
    profilesViewModel.updateProfile(
        user.copy(
            catchPhrase = catchphrase.text,
            description = description.text,
            tags = tagsViewModel.filteringTags.value))
  }

  // for tags display, I will use Samuel's tags viewProfile to load the tags
  val selectedTags = tagsViewModel.filteringTags.collectAsState()
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = tagsViewModel.query.collectAsState()

  val expanded = remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("profileEditScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag("topAppBar"),
            title = { Text("") },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag("goBackButton"),
                  onClick = {
                    // TODO : verify that the profile has been modified before showing the modal
                    showDialogBack = true
                    tagsViewModel.leaveUI()
                  }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  modifier = Modifier.testTag("checkButton"),
                  onClick = { showDialogConfirm = true }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                  }
            })
      }) {
        Column(
            modifier = Modifier.padding(it).padding(padding).testTag("profileEditScreenContent"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              AsyncImage( // TODO : implement image uploading
                  model = profilePicture,
                  contentDescription = "Profile Picture",
                  modifier =
                      Modifier.size(profilePictureSize)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primary)
                          .testTag("profilePicture")) // Added test tag

              Spacer(modifier = Modifier.height(padding))

              // Name Input
              Text(
                  text = "${user.name}, ${user.calculateAge()}",
                  style = TextStyle(fontSize = textSize.value.sp),
                  modifier =
                      Modifier.fillMaxWidth()
                          .wrapContentWidth(Alignment.CenterHorizontally)
                          .testTag("nameAndAge"))
              Spacer(modifier = Modifier.height(padding))

              // Catchphrase Input
              OutlinedTextField(
                  value = catchphrase,
                  onValueChange = { catchphrase = it },
                  label = { Text("Catchphrase", modifier = Modifier.testTag("catchphraseLabel")) },
                  textStyle = TextStyle(fontSize = textSize.value.sp * 0.6),
                  modifier =
                      Modifier.fillMaxWidth().height(catchphraseHeight).testTag("catchphrase"))

              Spacer(modifier = Modifier.height(padding))

              // Description Input
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  textStyle = TextStyle(fontSize = textSize.value.sp * 0.6),
                  modifier =
                      Modifier.fillMaxWidth().height(descriptionHeight).testTag("description"))
              Spacer(modifier = Modifier.height(padding))

              val tagSelectorHeight =
                  screenHeight * com.github.se.icebreakrr.ui.sections.tagSelectorHeightFactor
              TagSelector(
                  selectedTag =
                      selectedTags.value.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
                  outputTag =
                      tagsSuggestions.value.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
                  stringQuery = stringQuery.value,
                  expanded = expanded,
                  onTagClick = { tag -> tagsViewModel.removeFilter(tag) },
                  onStringChanged = { tagsViewModel.setQuery(it, selectedTags.value) },
                  textColor = MaterialTheme.colorScheme.onSurface,
                  onDropDownItemClicked = { tag -> tagsViewModel.addFilter(tag) },
                  height = tagSelectorHeight,
                  width = screenWidth,
                  textSize = (tagSelectorHeight.value * tagSelectorTextSizeFactor).sp)
              Spacer(modifier = Modifier.height(padding))

              // TODO: implement tags query and display with Maximo's tags UI and Samuel's
              // viewProfile

              // Modal for unsaved changes on leave page with back button
              if (showDialogBack) {
                AlertDialog(
                    onDismissRequest = { showDialogBack = false },
                    title = { Text("You are about to leave this page") },
                    text = { Text("Your changes will not be saved.") },
                    confirmButton = {
                      TextButton(
                          onClick = {
                            tagsViewModel.leaveUI()
                            navigationActions.goBack()
                          }) {
                            Text("Discard changes")
                          }
                    },
                    dismissButton = {
                      TextButton(onClick = { showDialogBack = false }) { Text("Cancel") }
                    },
                    modifier = Modifier.testTag("alertDialog"))
              } else if (showDialogConfirm) {
                AlertDialog(
                    onDismissRequest = { showDialogConfirm = false },
                    title = { Text("You are about to leave this page") },
                    text = { Text("Are you sure you want to save your changes?") },
                    confirmButton = {
                      TextButton(
                          onClick = {
                            updateProfile()
                            tagsViewModel.leaveUI()
                            navigationActions.goBack()
                          }) {
                            Text("Save")
                          }
                    },
                    dismissButton = {
                      TextButton(onClick = { showDialogConfirm = false }) { Text("Cancel") }
                    },
                    modifier = Modifier.testTag("alertDialogConfirm"))
              }
            }
      }
}
