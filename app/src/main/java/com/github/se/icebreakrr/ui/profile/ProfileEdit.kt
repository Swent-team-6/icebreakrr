package com.github.se.icebreakrr.ui.profile

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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.sections.tagSelectorTextSizeFactor
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditingScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModel.Factory)
) {

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  // Define dynamic padding and text size based on screen size
  val padding = screenWidth * 0.02f // 2% of screen width
  val textSize = screenWidth * 0.06f // 6% of screen width

  // TODO : make the padding and text size dynamic based on screen size

  val user =
      Profile(
          uid = "",
          name = "",
          gender = Gender.OTHER,
          birthDate = Timestamp.now(),
          catchPhrase = "",
          description = "",
          tags = listOf(),
          profilePictureUrl = "")

  val profilePicture by remember { mutableStateOf(user.profilePictureUrl) }
  var catchphrase by remember { mutableStateOf(TextFieldValue(user.catchPhrase)) }
  var description by remember { mutableStateOf(TextFieldValue(user.description)) }
  var showDialog by remember { mutableStateOf(false) }

  // to POST the new profile, I will use Arthur's viewModel

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
                    showDialog = true
                    tagsViewModel.leaveUI()
                  }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  modifier = Modifier.testTag("checkButton"),
                  onClick = {
                    // TODO : handle save action
                    tagsViewModel.leaveUI()
                  }) {
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
                      Modifier.size(100.dp)
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
                  modifier = Modifier.fillMaxWidth().height(60.dp).testTag("catchphrase"))

              Spacer(modifier = Modifier.height(padding))

              // Description Input
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  modifier = Modifier.fillMaxWidth().height(100.dp).testTag("description"))
              Spacer(modifier = Modifier.height(padding))

              // TODO: This is temporary for tests and will be replaced with the actual tag selector
              // of Maximo
              val tagSelectorHeight =
                  screenHeight * com.github.se.icebreakrr.ui.sections.tagSelectorHeightFactor
              val tagSelectorWidth =
                  screenWidth * com.github.se.icebreakrr.ui.sections.tagSelectorWidthFactor
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
                  width = tagSelectorWidth,
                  textSize = (tagSelectorHeight.value * tagSelectorTextSizeFactor).sp)
              Spacer(modifier = Modifier.height(padding))

              // TODO : This will be replaced by the actual tags
              Text(
                  "Tags",
                  style = MaterialTheme.typography.titleMedium,
                  modifier = Modifier.align(Alignment.Start).testTag("userTags"))

              // TODO: implement tags query and display with Maximo's tags UI and Samuel's
              // viewProfile

              // Modal for unsaved changes on leave page with back button
              if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("You are about to leave this page") },
                    text = { Text("Do you want to save your changes?") },
                    confirmButton = {
                      TextButton(
                          onClick = {
                            tagsViewModel.leaveUI()
                            navigationActions.goBack()
                          },
                          modifier = Modifier.testTag("discardChangesOption")) {
                            Text("Discard changes")
                          }
                    },
                    dismissButton = {
                      TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    },
                    modifier = Modifier.testTag("alertDialog"))
              }
            }
      }
}
