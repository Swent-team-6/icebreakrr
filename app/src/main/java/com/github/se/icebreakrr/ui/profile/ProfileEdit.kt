package com.github.se.icebreakrr.ui.profile

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel.ProfilePictureState.*
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.sections.shared.UnsavedChangesDialog
import com.github.se.icebreakrr.ui.sections.shared.handleSafeBackNavigation
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditingScreen(
    navigationActions: NavigationActions,
    tagsViewModel: TagsViewModel,
    profilesViewModel: ProfilesViewModel,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val padding = screenWidth * 0.02f
  val textSize = screenWidth * 0.08f

  val profilePictureSize = screenWidth * 0.40f
  val catchphraseHeight = screenHeight * 0.10f
  val descriptionHeight = screenHeight * 0.16f

  val CATCHPHRASE_MAX = 200
  val DESSCRIPTION_MAX = 400

  LaunchedEffect(Unit) {
    auth.currentUser?.let { profilesViewModel.getProfileByUid(it.uid) }
    profilesViewModel.selectedProfile.value?.tags?.forEach { tag -> tagsViewModel.addFilter(tag) }
  }

  val isLoading = profilesViewModel.loading.collectAsState(initial = true).value
  val pictureChangeState = profilesViewModel.pictureChangeState.collectAsState().value
  val user = profilesViewModel.selfProfile.collectAsState().value!!
  val editedCurrentProfile = profilesViewModel.editedCurrentProfile.collectAsState().value
  val tempBitmap = profilesViewModel.tempProfilePictureBitmap.collectAsState().value

  var catchphrase by remember {
    mutableStateOf(TextFieldValue(editedCurrentProfile?.catchPhrase ?: user.catchPhrase))
  }
  var description by remember {
    mutableStateOf(TextFieldValue(editedCurrentProfile?.description ?: user.description))
  }
  val expanded = remember { mutableStateOf(false) }

  var showDialog by remember { mutableStateOf(false) }
  var isModified by remember {
    mutableStateOf(editedCurrentProfile != null || pictureChangeState != UNCHANGED)
  }

  val selectedTags = tagsViewModel.filteringTags.collectAsState().value
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = remember { mutableStateOf("") }
  fun getEditedProfile(): Profile {
    return user.copy(
        catchPhrase = catchphrase.text, description = description.text, tags = selectedTags)
  }

  if (isLoading) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).testTag("loadingBox"),
        contentAlignment = Alignment.Center) {
          Text("Loading profile...", textAlign = TextAlign.Center)
        }
  } else {

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
                      handleSafeBackNavigation(
                          isModified = isModified,
                          setShowDialog = { showDialog = it },
                          tagsViewModel = tagsViewModel,
                          navigationActions = navigationActions,
                          profilesViewModel = profilesViewModel)
                    }) {
                      Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
              },
              actions = {
                IconButton(
                    modifier = Modifier.testTag("checkButton"),
                    onClick = {
                      if (isModified) {
                        profilesViewModel.saveEditedProfile(getEditedProfile())
                        profilesViewModel.validateProfileChanges(context)
                      }
                      tagsViewModel.leaveUI()
                      navigationActions.goBack() // todo: block with loading until done (uploading)
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
                    pictureChangeState = pictureChangeState,
                    size = profilePictureSize,
                    onSelectionSuccess = { uri ->
                      profilesViewModel.generateTempProfilePictureBitmap(context, uri)
                      // save modification and implicitly sets isModified at next recomposition:
                      profilesViewModel.saveEditedProfile(getEditedProfile())
                      navigationActions.navigateTo(Screen.CROP)
                    },
                    onSelectionFailure = {
                      Toast.makeText(context, "Failed to select image", Toast.LENGTH_SHORT).show()
                    },
                    onDeletion = {
                      when (pictureChangeState) {
                        UNCHANGED -> { // delete current profile picture
                          if (user.profilePictureUrl != null) {
                            profilesViewModel.setPictureChangeState(TO_DELETE)
                            isModified = true
                          }
                        }
                        // else cancel changes
                        else -> profilesViewModel.setPictureChangeState(UNCHANGED)
                      }
                    })

                Spacer(modifier = Modifier.height(padding))

                // Name Input
                Text(
                    text = user.name,
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
                      if (it.text.length <= CATCHPHRASE_MAX) {
                        catchphrase = it
                        isModified = true
                      }
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
                      if (it.text.length <= DESSCRIPTION_MAX) {
                        description = it
                        isModified = true
                      }
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
                    textSize = (screenHeight.value * 0.03).sp,
                )

                Spacer(modifier = Modifier.height(padding))

                UnsavedChangesDialog(
                    showDialog = showDialog && isModified,
                    onDismiss = { showDialog = false },
                    onConfirm = {
                      showDialog = false
                      tagsViewModel.leaveUI()
                      profilesViewModel.resetProfileEditionState()
                      navigationActions.goBack()
                    })
              }
        }
  }

  // allows the user to navigate back safely with system back button
  BackHandler {
    handleSafeBackNavigation(
        isModified = isModified,
        setShowDialog = { showDialog = it },
        tagsViewModel = tagsViewModel,
        navigationActions = navigationActions,
        profilesViewModel = profilesViewModel)
  }
}
