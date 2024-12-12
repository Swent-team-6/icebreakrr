package com.github.se.icebreakrr.model.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.utils.IPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// This file was written with the help of Cursor AI

private const val CHECK_INTERVAL = 5 * 60 * 1_000_000L // 5 minutes
private const val NOTIFICATION_COOLDOWN = 4 * 60 * 60 * 1000L // 4 hours in milliseconds

/**
 * Manages engagement notifications between users based on proximity and shared interests.
 *
 * This class is responsible for:
 * - Monitoring nearby users based on the user's filter settings
 * - Detecting when users with common tags are within range
 * - Sending notifications when matches are found (only when app is in background)
 * - Respecting user's discoverability settings
 *
 * The manager runs periodic checks to find potential matches and sends notifications to both users
 * when they share common tags. It uses the same filtering criteria as the "Around You" screen to
 * maintain consistency in the user experience.
 *
 * @property profilesViewModel Handles profile data and filtering
 * @property meetingRequestViewModel Manages sending notifications
 * @property appDataStore Manages user preferences and settings
 * @property context Android context for system services
 * @property filterViewModel Manages user's filter settings (radius, age, gender)
 */
class EngagementNotificationManager(
    private val profilesViewModel: ProfilesViewModel,
    private val meetingRequestViewModel: MeetingRequestViewModel,
    private val appDataStore: AppDataStore,
    private val context: Context,
    private val filterViewModel: FilterViewModel,
    private val permissionManager: IPermissionManager
) {
  private var notificationJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Main)
  private val lastNotificationTimes = mutableMapOf<String, Long>()

  /**
   * Starts monitoring nearby users with common tags. Ensures POST_NOTIFICATIONS permission on
   * Android TIRAMISU+ and launches monitoring in a coroutine.
   */
  fun startMonitoring() {
    stopMonitoring() // Stop any existing monitoring

    // Handle permission request
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val permission = Manifest.permission.POST_NOTIFICATIONS
      val permissionStatus = permissionManager.permissionStatuses.value[permission]
      if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
        permissionManager.requestPermissions(arrayOf(permission))
      }
    }

    notificationJob =
        scope.launch {
          // while (true) {
          checkNearbyUsersForCommonTags()
          // delay(CHECK_INTERVAL)
          // }
        }
  }

  /** Stop monitoring for nearby users */
  fun stopMonitoring() {
    notificationJob?.cancel()
    notificationJob = null
  }

  /**
   * Checks for nearby users with common tags and processes them for potential engagement
   * notifications.
   *
   * This function retrieves the user's profile and location, then uses the filter settings to find
   * nearby profiles within the specified radius. It launches a coroutine to collect these profiles
   * and processes them if the user is discoverable.
   */
  private fun checkNearbyUsersForCommonTags() {
    profilesViewModel.getSelfProfile {
      val selfProfile = profilesViewModel.selfProfile.value ?: return@getSelfProfile
      val selfLocation = selfProfile.location ?: return@getSelfProfile

      // Launch a coroutine to collect the filtered profiles
      scope.launch {
        // Only proceed if we are discoverable
        if (!appDataStore.isDiscoverable.first()) return@launch

        profilesViewModel.filteredProfiles.collectLatest { nearbyProfiles ->
          // Process all nearby profiles
          processNearbyProfiles(selfProfile, nearbyProfiles)
        }
      }
    }
  }

  /**
   * Processes a list of nearby profiles to find common tags and send engagement notifications.
   *
   * @param selfProfile The user's own profile containing their tags.
   * @param nearbyProfiles A list of profiles that are within the user's selected radius.
   *
   * This function filters out the user's own profile from the list, then iterates over the
   * remaining profiles to find common tags. If common tags are found, it sends a notification to
   * the nearby user using the first common tag.
   */
  private fun processNearbyProfiles(selfProfile: Profile, nearbyProfiles: List<Profile>) {
    val selfTags = selfProfile.tags
    val newListMinusSelf = nearbyProfiles.filter { it != selfProfile }

    for (nearbyProfile in newListMinusSelf) {
      // Skip if we've recently notified this user
      val lastTime = lastNotificationTimes[nearbyProfile.uid] ?: 0L
      if (System.currentTimeMillis() - lastTime < NOTIFICATION_COOLDOWN) continue

      // Find common tags
      val commonTags = selfTags.intersect(nearbyProfile.tags.toSet())

      if (commonTags.isNotEmpty()) {
        // Send notification for the first common tag
        val commonTag = commonTags.first()
        try {
          meetingRequestViewModel.engagementNotification(
              targetToken = nearbyProfile.fcmToken ?: "null", tag = commonTag)
          // Record notification time
          lastNotificationTimes[nearbyProfile.uid] = System.currentTimeMillis()
        } catch (e: Exception) {
          Log.e("EngagementNotification", "Failed to send notification", e)
        }
      }
    }
  }

  // Add this method for testing purposes
  @VisibleForTesting fun isMonitoring(): Boolean = notificationJob?.isActive == true
}
