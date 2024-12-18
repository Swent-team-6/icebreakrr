package com.github.se.icebreakrr.authentication

import android.content.Context
import android.util.Log
import com.github.se.icebreakrr.R
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.location.LocationViewModel
import com.github.se.icebreakrr.model.notification.EngagementNotificationManager
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * Logs out the user from Firebase and Google Sign-In, and navigates to the authentication screen.
 *
 * This function:
 * - Signs out the user from Firebase to clear the session.
 * - Configures and initializes GoogleSignInClient to handle Google account sign-out.
 * - Signs out from Google and revokes access, forcing account re-selection on next login.
 * - Navigates to the authentication screen if logout is successful; logs an error otherwise.
 *
 * @param context Used to initialize GoogleSignInClient.
 * @param navigationActions Handles navigation to the authentication screen.
 */
fun logout(
    context: Context,
    navigationActions: NavigationActions,
    appDataStore: AppDataStore,
    engagementManager: EngagementNotificationManager,
    locationViewModel: LocationViewModel
) {
  // Stop monitoring engagement before logout
  engagementManager.stopMonitoring()

  // Stop updating location before logout
  locationViewModel.stopLocationUpdates()

  // Initialize FirebaseAuth
  val auth = FirebaseAuth.getInstance()

  // Configure GoogleSignIn
  val gso =
      GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestIdToken(context.getString(R.string.default_web_client_id))
          .requestEmail()
          .build()

  val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

  // Clear stored auth token
  kotlinx.coroutines.runBlocking { appDataStore.clearAuthToken() }

  // Sign out from Firebase
  auth.signOut()

  // Sign out from Google and reset the google account choice
  googleSignInClient.signOut().addOnCompleteListener { signOutTask ->
    if (signOutTask.isSuccessful) {
      googleSignInClient.revokeAccess().addOnCompleteListener { _ ->
        navigationActions.navigateTo(Screen.AUTH)
      }
    } else { // On error
      Log.e("GoogleSignIn", "Failed to sign out from Google.")
    }
  }
}
