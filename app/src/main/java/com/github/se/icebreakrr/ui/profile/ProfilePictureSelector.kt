package com.github.se.icebreakrr.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.profile.ProfilesViewModel.ProfilePictureState.*
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue

/**
 * A composable function that displays a profile picture with an option to select a new image from
 * gallery.
 *
 * @param url The URL of the profile picture to display. If null, a placeholder image is shown.
 * @param size The size of the profile picture.
 * @param onSelectionSuccess A callback function that is invoked when an image is successfully
 *   selected.
 * @param onSelectionFailure A callback function that is invoked when image selection fails.
 */
private val ICON_PADDING = 5.dp

@Composable
fun ProfilePictureSelector(
    url: String?,
    localBitmap: Bitmap?,
    pictureChangeState: ProfilesViewModel.ProfilePictureState,
    size: Dp,
    onSelectionSuccess: (Uri) -> Unit,
    onSelectionFailure: () -> Unit,
    onDeletion: () -> Unit,
) {
  // Create an image picker launcher for selecting an image from the gallery
  val imagePicker =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.PickVisualMedia(),
          onResult = { uri ->
            if (uri != null) {
              onSelectionSuccess(uri)
            } else {
              onSelectionFailure()
            }
          })

  // The outer box is used to contain the clickable part for edition and the cancel/remove icon
  Box {
    // Create a Box composable to contain the profile picture and the add photo icon
    Box(
        modifier =
            Modifier.clickable(
                    onClick = {
                      imagePicker.launch(
                          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    })
                .size(size)
                .background(MaterialTheme.colorScheme.background)
                .testTag("profilePicture")) {
          // Display the profile picture if the URL is not null
          AsyncImage(
              model =
                  when (pictureChangeState) {
                    UNCHANGED -> url
                    TO_UPLOAD -> localBitmap
                    TO_DELETE -> null
                  },
              contentDescription = "your profile picture",
              placeholder = painterResource(id = R.drawable.nopp),
              error = painterResource(id = R.drawable.nopp),
              modifier = Modifier.size(size).clip(CircleShape))

          // Display an edit icon to show that the user can change the profile picture
          Icon(
              Icons.Filled.Create,
              contentDescription = "Add a photo",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier =
                  Modifier.size(size / 4)
                      .clip(CircleShape)
                      .background(IceBreakrrBlue)
                      .padding(ICON_PADDING)
                      .align(Alignment.BottomEnd)
                      .testTag("addPhotoIcon"))
        }

    // A button to remove the picture or cancel changes
    Icon(
        imageVector =
            if (pictureChangeState == UNCHANGED) Icons.Default.Delete else Icons.Default.Clear,
        contentDescription = "Remove photo",
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier =
            Modifier.size(size / 4)
                .clip(CircleShape)
                .background(DarkGray)
                .padding(ICON_PADDING)
                .align(Alignment.TopEnd)
                .testTag("removePhotoIcon")
                .clickable { onDeletion() })
  }
}
