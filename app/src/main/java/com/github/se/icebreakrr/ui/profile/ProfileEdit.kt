package com.github.se.icebreakrr.ui.profile

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditingScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel,
    profilesViewModel: ProfilesViewModel
) {

  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val padding = screenWidth * 0.02f
  val textSize = screenWidth * 0.08f

  val profilePictureSize = screenWidth * 0.25f
  val catchphraseHeight = screenHeight * 0.10f
  val descriptionHeight = screenHeight * 0.16f

  LaunchedEffect(Unit) {
    Firebase.auth.currentUser?.let { profilesViewModel.getProfileByUid(it.uid) }
  }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val user = profilesViewModel.selectedProfile.collectAsState().value
  val tempBitmap = profilesViewModel.tempProfilePictureBitmap.collectAsState().value

  LaunchedEffect(user?.tags) { user?.tags?.forEach { tag -> tagsViewModel.addFilter(tag) } }

  var catchphrase by remember { mutableStateOf(TextFieldValue(user!!.catchPhrase)) }
  var description by remember { mutableStateOf(TextFieldValue(user!!.description)) }
  val expanded = remember { mutableStateOf(false) }

  var showDialog by remember { mutableStateOf(false) }
  var isModified by remember { mutableStateOf(false) }

  val selectedTags = tagsViewModel.filteringTags.collectAsState().value
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = tagsViewModel.query.collectAsState()

  fun updateProfile() {
    profilesViewModel.updateProfile(
        user!!.copy(
            catchPhrase = catchphrase.text, description = description.text, tags = selectedTags))
  }

  if (isLoading) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.LightGray).testTag("loadingBox"),
        contentAlignment = Alignment.Center) {
          Text("Loading profile...", textAlign = TextAlign.Center)
        }
  } else if (user != null) {

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
                      if (isModified) {
                        showDialog = true
                      } else {
                        tagsViewModel.leaveUI()
                        navigationActions.goBack()
                      }
                    }) {
                      Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
              },
              actions = {
                IconButton(
                    modifier = Modifier.testTag("checkButton"),
                    onClick = {
                      updateProfile()
                      profilesViewModel.validateAndUploadProfilePicture(context)
                      tagsViewModel.leaveUI()
                      navigationActions.goBack()
                    }) {
                      Icon(Icons.Default.Check, contentDescription = "Save")
                    }
              })
        }) {
          Column(
              modifier = Modifier.padding(it).padding(padding).testTag("profileEditScreenContent"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // A composable that allows the user to preview and edit a profile picture
                ProfilePictureSelector(
                    url = user.profilePictureUrl,
                    localBitmap = tempBitmap,
                    size = profilePictureSize,
                    onSelectionSuccess = { uri ->
                      profilesViewModel.generateTempProfilePictureBitmap(context, uri)
                      isModified = true
                    },
                    onSelectionFailure = {
                      Toast.makeText(context, "Failed to select image", Toast.LENGTH_SHORT).show()
                    })

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
                    onValueChange = {
                      catchphrase = it
                      isModified = true
                    },
                    label = {
                      Text("Catchphrase", modifier = Modifier.testTag("catchphraseLabel"))
                    },
                    textStyle = TextStyle(fontSize = textSize.value.sp * 0.6),
                    modifier =
                        Modifier.fillMaxWidth().height(catchphraseHeight).testTag("catchphrase"))

                Spacer(modifier = Modifier.height(padding))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                      description = it
                      isModified = true
                    },
                    label = { Text("Description") },
                    textStyle = TextStyle(fontSize = textSize.value.sp * 0.6),
                    modifier =
                        Modifier.fillMaxWidth().height(descriptionHeight).testTag("description"))
                Spacer(modifier = Modifier.height(padding))

                TagSelector(
                    selectedTag =
                        selectedTags.map { tag -> Pair(tag, tagsViewModel.tagToColor(tag)) },
                    outputTag =
                        tagsSuggestions.value.map { tag ->
                          Pair(tag, tagsViewModel.tagToColor(tag))
                        },
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
                    textSize = (screenHeight.value * 0.03).sp,
                )

                Spacer(modifier = Modifier.height(padding))

                // Modal for unsaved changes on leave page with back button
                if (showDialog && isModified) {
                  AlertDialog(
                      onDismissRequest = { showDialog = false },
                      title = { Text("You are about to leave this page") },
                      text = { Text("Your changes will not be saved.") },
                      confirmButton = {
                        TextButton(
                            onClick = {
                              showDialog = false
                              tagsViewModel.leaveUI()
                              profilesViewModel.clearTempProfilePictureBitmap()
                              navigationActions.goBack()
                            }) {
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
}
