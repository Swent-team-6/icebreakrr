package com.github.se.icebreakrr.ui.authentication

// This file contains the SignIn screen implementation using Jetpack Compose.
// It handles user authentication with Google Sign-In via Firebase Auth.
// The code has been highly inspired by the bootcamp examples and modified for this project.

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.config.LocalIsTesting
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.TopLevelDestinations
import com.github.se.icebreakrr.utils.NetworkUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Composable function that displays the SignIn screen. This screen handles the user authentication
 * flow with Google Sign-In via Firebase.
 *
 * @param navigationActions A class that handles the navigation between screens.
 */
@Composable
fun SignInScreen(profilesViewModel: ProfilesViewModel, navigationActions: NavigationActions) {

  // State to hold the current Firebase user
  var user by remember { mutableStateOf(Firebase.auth.currentUser) }
  // Retrieve the token from resources (needed for Google Sign-In)
  val token = stringResource(R.string.default_web_client_id)
  // Get the context to use within the composable
  val context = LocalContext.current

  // Check if the app is running in test mode
  val isTesting = LocalIsTesting.current

  // Get screen configuration to dynamically adjust layout
  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp

  // Define padding and spacing as percentages of the screen height
  val verticalPadding = (screenHeight * 0.2).dp // 20%

  val coroutineScope = rememberCoroutineScope()

  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            user = result.user
            user?.let { firebaseUser ->
              coroutineScope.launch {

                // Checking if user already exists
                profilesViewModel.getProfileByUid(firebaseUser.uid)

                // Wait until loading becomes false which means that we got the user
                profilesViewModel.loading.first { !it }

                // Check selectedProfile after loading completes
                val profile = profilesViewModel.selectedProfile.value

                // checking if profile already exists
                if (profile == null) { // if doesn't exist create new user

                  val newProfile =
                      Profile(
                          uid = firebaseUser.uid,
                          name = firebaseUser.displayName ?: "Unknown",
                          gender = Gender.OTHER,
                          birthDate = Timestamp.now(),
                          catchPhrase = "",
                          description = "")

                  profilesViewModel.addNewProfile(newProfile)
                }

                // Navigate to sign in screen after completion
                navigationActions.navigateTo(TopLevelDestinations.AROUND_YOU)
              }
            }
          },
          onAuthError = { user = null })

  // Define a linear gradient with the provided colors
  val gradientBrush =
      Brush.linearGradient(
          colors =
              listOf(
                  Color(0xFF1FAEF0), // Light blue
                  Color(0xFF1C9EDA), // Mid blue
                  Color(0xFF12648A) // Dark blue
                  ))

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("loginScreen"),
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.background(brush = gradientBrush)
                    .fillMaxSize()
                    .padding(vertical = verticalPadding)
                    .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
              modifier = Modifier.testTag("loginTitle"),
              text = "IceBreakrr",
              style =
                  TextStyle(
                      fontSize = 64.sp,
                      lineHeight = 25.07.sp,
                      fontWeight = FontWeight(500),
                      color = Color(0xFFFFFFFF),
                      textAlign = TextAlign.Center,
                      letterSpacing = 0.37.sp,
                  ))

          // Authenticate With Google Button
          GoogleSignInButton(
              onClick = {
                if (isTesting) {
                  navigationActions.navigateTo(TopLevelDestinations.AROUND_YOU)
                } else {
                  if (!NetworkUtils.isNetworkAvailable(context)) {
                    NetworkUtils.showNoInternetToast(context)
                  } else {
                    val gso =
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(token)
                            .requestEmail()
                            .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)
                  }
                }
              })
        }
      })
}

/**
 * Composable function to display the Google Sign-In button. It is a custom-styled button that
 * triggers the Google Sign-In flow when clicked.
 *
 * @param onSignInClick Callback invoked when the button is clicked.
 */
@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
  Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Button color
      shape = RoundedCornerShape(50), // Circular edges for the button
      border = BorderStroke(1.dp, Color.LightGray),
      modifier =
          Modifier.padding(8.dp)
              .height(48.dp) // Adjust height as needed
              .testTag("loginButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter =
                      painterResource(id = R.drawable.google_logo), // Ensure this drawable exists
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(30.dp) // Size of the Google logo
                          .padding(end = 8.dp))

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  color = Color.Gray, // Text color
                  fontSize = 16.sp, // Font size
                  fontWeight = FontWeight.Medium)
            }
      }
}

/**
 * Sets up the Firebase authentication launcher for Google Sign-In. This launcher handles the result
 * of the Google Sign-In flow and performs Firebase authentication.
 *
 * @param onAuthComplete Callback when authentication is successful.
 * @param onAuthError Callback when authentication fails.
 * @return A launcher to handle the Google Sign-In intent.
 */
@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  // Coroutine scope for handling asynchronous tasks
  val scope = rememberCoroutineScope()

  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
      scope.launch {
        val authResult = Firebase.auth.signInWithCredential(credential).await()
        onAuthComplete(authResult)
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}
