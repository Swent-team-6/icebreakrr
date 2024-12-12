package com.github.se.icebreakrr.model.notification

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.filter.FilterViewModel
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.tags.TagsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "EngagementManager"
private const val CHECK_INTERVAL = 1 * 60 * 1000L // 1 minutes in milliseconds
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
    private val filterViewModel: FilterViewModel,
    private val tagsViewModel: TagsViewModel
) {
  private var notificationJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  private val lastNotificationTimes = mutableMapOf<String, Long>()

  /** Start monitoring for nearby users with common tags */
  fun startMonitoring() {
    Log.e(TAG, "Starting engagement monitoring")
    stopMonitoring() // Stop any existing monitoring

    notificationJob =
        scope.launch {
          Log.e(TAG, "Starting periodic checks")
          try {
            while (true) {
              Log.e(TAG, "Running periodic check")
              checkNearbyUsersForCommonTags()
              Log.e(TAG, "Waiting for next check interval (${CHECK_INTERVAL}ms)")
              delay(CHECK_INTERVAL)
            }
          } catch (e: Exception) {
            Log.e(TAG, "Error in monitoring loop: ${e.message}", e)
          }
        }
  }

  /** Stop monitoring for nearby users */
  fun stopMonitoring() {
    Log.e(TAG, "Stopping engagement monitoring")
    notificationJob?.let {
      it.cancel()
      Log.e(TAG, "Notification job cancelled")
    }
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
    Log.e(TAG, "Checking for nearby users")
    scope.launch {
      try {
        // Check if discoverable
        val isDiscoverable = appDataStore.isDiscoverable.first()
        Log.e(TAG, "User discoverable status: $isDiscoverable")
        if (!isDiscoverable) {
          Log.e(TAG, "User is not discoverable, skipping check")
          return@launch
        }

        // Get self profile
        profilesViewModel.getSelfProfile {
          val selfProfile = profilesViewModel.selfProfile.value
          if (selfProfile == null) {
            Log.e(TAG, "Self profile is null")
            return@getSelfProfile
          }
          val selfLocation = selfProfile.location
          if (selfLocation == null) {
            Log.e(TAG, "Self location is null")
            return@getSelfProfile
          }
          Log.e(
              TAG, "Got self profile with ${selfProfile.tags.size} tags at location $selfLocation")

          // Update filtered profiles
          Log.e(TAG, "Updating filtered profiles")
          profilesViewModel.getFilteredProfilesInRadius(
              center = selfLocation,
              radiusInMeters = filterViewModel.selectedRadius.value,
              genders = filterViewModel.selectedGenders.value,
              ageRange = filterViewModel.ageRange.value,
              tags = tagsViewModel.filteredTags.value)

          // Wait a bit for profiles to be fully loaded, then process them once
          scope.launch {
            delay(200) // Wait for profiles to stabilize
            val nearbyProfiles = profilesViewModel.filteredProfiles.value
            Log.e(TAG, "Found ${nearbyProfiles.size} nearby profiles")
            processNearbyProfiles(selfProfile, nearbyProfiles)
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error checking nearby users: ${e.message}", e)
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
    Log.e(TAG, "Processing ${nearbyProfiles.size} nearby profiles")
    val selfTags = selfProfile.tags
    val newListMinusSelf = nearbyProfiles.filter { it != selfProfile }
    Log.e(TAG, "Found ${newListMinusSelf.size} profiles (excluding self)")

    for (nearbyProfile in newListMinusSelf) {
      try {
        // Check cooldown
        val lastTime = lastNotificationTimes[nearbyProfile.uid] ?: 0L
        val timeSinceLastNotification = System.currentTimeMillis() - lastTime
        if (timeSinceLastNotification < NOTIFICATION_COOLDOWN) {
          Log.e(
              TAG,
              "Skipping profile ${nearbyProfile.uid} - cooldown active (${timeSinceLastNotification}ms < ${NOTIFICATION_COOLDOWN}ms)")
          continue
        }

        // Find common tags
        val commonTags = selfTags.intersect(nearbyProfile.tags.toSet())
        Log.e(TAG, "Found ${commonTags.size} common tags with profile ${nearbyProfile.uid}")

        if (commonTags.isNotEmpty()) {
          val commonTag = commonTags.first()
          Log.e(TAG, "Sending notification for common tag: $commonTag")
          try {
            meetingRequestViewModel.engagementNotification(
                targetToken = nearbyProfile.fcmToken ?: "null", tag = commonTag)
            lastNotificationTimes[nearbyProfile.uid] = System.currentTimeMillis()
            Log.e(TAG, "Successfully sent notification to ${nearbyProfile.uid}")
          } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification to ${nearbyProfile.uid}: ${e.message}", e)
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error processing profile ${nearbyProfile.uid}: ${e.message}", e)
      }
    }
  }

  // Add this method for testing purposes
  @VisibleForTesting fun isMonitoring(): Boolean = notificationJob?.isActive == true
}
