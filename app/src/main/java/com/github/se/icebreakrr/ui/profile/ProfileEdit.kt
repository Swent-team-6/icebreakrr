package com.github.se.icebreakrr.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.tags.TagSelector
import com.github.se.icebreakrr.ui.sections.shared.UnsavedChangesDialog
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

  LaunchedEffect(user?.tags) { user?.tags?.forEach { tag -> tagsViewModel.addFilter(tag) } }

  var profilePictureUrl by remember { mutableStateOf(user!!.profilePictureUrl) }
  var catchphrase by remember { mutableStateOf(TextFieldValue(user!!.catchPhrase)) }
  var description by remember { mutableStateOf(TextFieldValue(user!!.description)) }
  val expanded = remember { mutableStateOf(false) }

  var showDialog by remember { mutableStateOf(false) }
  var isMofidied by remember { mutableStateOf(false) }

  val selectedTags = tagsViewModel.filteringTags.collectAsState().value
  val tagsSuggestions = tagsViewModel.tagsSuggestions.collectAsState()
  val stringQuery = tagsViewModel.query.collectAsState()

  fun updateProfile() {
    profilesViewModel.updateProfile(
        Profile(
            uid = user!!.uid,
            name = user.name,
            gender = user.gender,
            birthDate = user.birthDate,
            catchPhrase = catchphrase.text,
            description = description.text,
            tags = selectedTags,
            profilePictureUrl = profilePictureUrl))
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
                      if (isMofidied) {
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
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(profilePictureSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("profilePicture"))

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
                      isMofidied = true
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
                      isMofidied = true
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
                      isMofidied = true
                    },
                    onStringChanged = {
                      tagsViewModel.setQuery(it, selectedTags)
                      isMofidied = true
                    },
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onDropDownItemClicked = { tag ->
                      tagsViewModel.addFilter(tag)
                      isMofidied = true
                    },
                    height = screenHeight,
                    width = screenWidth,
                    textSize = (screenHeight.value * 0.03).sp,
                )

                Spacer(modifier = Modifier.height(padding))

                UnsavedChangesDialog(
                    showDialog = showDialog && isMofidied,
                    onDismiss = { showDialog = false },
                    onConfirm = {
                        showDialog = false
                        tagsViewModel.leaveUI()
                        navigationActions.goBack()
                    }
                )
              }
        }
  }
}
