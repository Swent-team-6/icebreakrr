package com.github.se.icebreakrr.ui.profile

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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue

@Preview(showBackground = true)
@Composable
fun ProfilePicturePreview() {
    ProfilePicture(
        url = null,
        size = 100.dp,
        onSelectionSuccess = {},
        onSelectionFailure = {})
}

@Composable
fun ProfilePicture(
    url: String?,
    size: Dp,
    onSelectionSuccess: (Uri) -> Unit,
    onSelectionFailure: () -> Unit ) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSelectionSuccess(uri)
            } else {
                onSelectionFailure()
            }
        }
    )

    Box(
        modifier = Modifier
            .clickable(onClick = {
                imagePicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            })
            .size(size)
            .background(MaterialTheme.colorScheme.background)
            .testTag("profilePicture")
    ) {
        AsyncImage(
            model = url,
            contentDescription = "your profile picture",
            placeholder = painterResource(id = R.drawable.nopp), // Default image during loading
            error = painterResource(id = R.drawable.nopp), // Fallback image if URL fails
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )

        Icon(
            Icons.Filled.Create,
            contentDescription = "Add a photo",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(size / 3)
                .clip(CircleShape)
                .background(IceBreakrrBlue)
                .padding(5.dp)
                .align(Alignment.BottomEnd)
                .testTag("addPhotoIcon")
        )
    }
}



