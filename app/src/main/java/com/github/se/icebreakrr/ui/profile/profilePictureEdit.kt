import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import com.smarttoolfactory.cropper.settings.CropProperties
import com.smarttoolfactory.cropper.settings.CropStyle
import com.smarttoolfactory.cropper.settings.CropType

/**
 * Composable function for the Image Cropper Screen.
 *
 * @param profilesViewModel The ViewModel that holds the profile data.
 * @param navigationActions The navigation actions to handle screen transitions.
 */
@Composable
fun ImageCropperScreen(
    profilesViewModel: ProfilesViewModel,
    navigationActions: NavigationActions,
) {
  // Remember the temporary bitmap from the ViewModel
  val tempBitmap = remember { profilesViewModel.tempProfilePictureBitmap.value }

  // Create parameters for the ImageCropper
  val cropProperties = remember {
    CropDefaults.properties(
        cropType = CropType.Static,
        handleSize = 100F,
        cropOutlineProperty = CropOutlineProperty(OutlineType.Oval, RectCropShape(0, "rect")),
        aspectRatio = AspectRatio(1f), // 1:1 aspect ratio
        fixedAspectRatio = true,
    )
  }
  val cropStyle = remember { CropDefaults.style() }

  // Box to center the ImageCropper
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    ImageCropperWrapper(
        tempBitmap = tempBitmap!!.asImageBitmap(),
        cropProperties = cropProperties,
        cropStyle = cropStyle,
        onCropSuccess = { imageBitmap ->
          profilesViewModel.processCroppedImage(imageBitmap.asAndroidBitmap())
          navigationActions.goBack()
        })
  }
}

/**
 * Wrapper composable function for the Image Cropper.
 *
 * @param tempBitmap The bitmap image to be cropped.
 * @param cropProperties The properties for the cropping operation.
 * @param cropStyle The style for the cropping UI.
 * @param onCropSuccess Callback function to handle the cropped image.
 */
@Composable
fun ImageCropperWrapper(
    tempBitmap: ImageBitmap,
    cropProperties: CropProperties,
    cropStyle: CropStyle,
    onCropSuccess: (ImageBitmap) -> Unit,
) {
  // Local crop state, scoped to ImageCropper
  var crop by remember { mutableStateOf(false) }

  Box {
    ImageCropper(
        modifier = Modifier.fillMaxSize(),
        imageBitmap = tempBitmap,
        contentDescription = "Profile Picture",
        cropProperties = cropProperties,
        cropStyle = cropStyle,
        crop = crop, // This state only affects the ImageCropper
        onCropStart = {},
        onCropSuccess = { imageBitmap ->
          crop = false
          onCropSuccess(imageBitmap) // Callback to propagate the result
        })

    // Button to trigger cropping, placed inside the wrapper
    Button(
        onClick = { crop = true },
        modifier = Modifier.align(Alignment.BottomCenter).padding(vertical = 50.dp),
    ) {
      Text(
          text = "Crop",
          fontSize = 30.sp,
          modifier = Modifier.align(Alignment.CenterVertically).padding(10.dp),
      )
    }
  }
}
