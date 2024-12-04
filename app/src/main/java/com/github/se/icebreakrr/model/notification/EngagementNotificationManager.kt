package com.github.se.icebreakrr.model.notification

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.github.se.icebreakrr.data.AppDataStore
import com.github.se.icebreakrr.model.message.MeetingRequestViewModel
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.github.se.icebreakrr.model.filter.FilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//This file was written with the help of Cursor AI

private const val CHECK_INTERVAL = 5*60*1_000_000L // 5 minutes

/**
 * Manages engagement notifications between users based on proximity and shared interests.
 * 
 * This class is responsible for:
 * - Monitoring nearby users based on the user's filter settings 
 * - Detecting when users with common tags are within range
 * - Sending notifications when matches are found (only when app is in background)
 * - Respecting user's discoverability settings
 *
 * The manager runs periodic checks to find potential matches and sends
 * notifications to both users when they share common tags. It uses the same filtering
 * criteria as the "Around You" screen to maintain consistency in the user experience.
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
    private val filterViewModel: FilterViewModel
) {
    private var notificationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Start monitoring for nearby users with common tags
     */
    fun startMonitoring() {
        stopMonitoring() // Stop any existing monitoring

        notificationJob = scope.launch {
            while (true) {
                checkNearbyUsersForCommonTags()
                delay(CHECK_INTERVAL)
            }
        }
    }

    /**
     * Stop monitoring for nearby users
     */
    fun stopMonitoring() {
        notificationJob?.cancel()
        notificationJob = null
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun checkNearbyUsersForCommonTags() {
        val selfProfile = profilesViewModel.selfProfile.value ?: return
        val selfLocation = selfProfile.location ?: return

        // Use the user's selected radius from FilterViewModel
        profilesViewModel.getFilteredProfilesInRadius(
            center = selfLocation,
            radiusInMeters = filterViewModel.selectedRadius.value,
            genders = filterViewModel.selectedGenders.value,
            ageRange = filterViewModel.ageRange.value
        )

        // Launch a coroutine to collect the filtered profiles
        scope.launch {
            // Only proceed if we are discoverable
            if (!appDataStore.isDiscoverable.first()) return@launch

            profilesViewModel.filteredProfiles.collectLatest { nearbyProfiles ->
                // Only send notifications if the app is not in foreground
                if (!isAppInForeground()) {
                    processNearbyProfiles(selfProfile, nearbyProfiles)
                }
            }
        }
    }

    private fun processNearbyProfiles(selfProfile: Profile, nearbyProfiles: List<Profile>) {
        val selfTags = selfProfile.tags

        for (nearbyProfile in nearbyProfiles) {
            if (nearbyProfile.uid == selfProfile.uid) continue // Skip self

            // Find common tags
            val commonTags = selfTags.intersect(nearbyProfile.tags.toSet())
            
            if (commonTags.isNotEmpty()) {
                // Send notification for the first common tag
                val commonTag = commonTags.first()
                try {
                    // Send notification to the other user
                    meetingRequestViewModel.engagementNotification(
                        targetToken = nearbyProfile.fcmToken ?: continue,
                        tag = commonTag
                    )
                    
                    // Send notification to self as well
                    meetingRequestViewModel.engagementNotification(
                        targetToken = selfProfile.fcmToken ?: continue,
                        tag = commonTag
                    )
                } catch (e: Exception) {
                    Log.e("EngagementNotification", "Failed to send notification", e)
                }
            }
        }
    }
} 