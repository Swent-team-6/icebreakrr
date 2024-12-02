package com.github.se.icebreakrr.model.profile

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar

/**
 * Data class representing a user's profile.
 *
 * This class encapsulates the various attributes of a user's profile, including personal details,
 * location, tags, and interaction data such as meeting requests and blocked users.
 *
 * @property uid The unique identifier for the profile.
 * @property name The name of the user.
 * @property gender The user's gender.
 * @property birthDate The birth date of the user as a [Timestamp].
 * @property catchPhrase The user's catchphrase or tagline.
 * @property description A brief description of the user.
 * @property tags A list of tags associated with the user, useful for matching or categorization.
 * @property profilePictureUrl The URL of the user's profile picture (optional).
 * @property fcmToken The Firebase Cloud Messaging (FCM) token for push notifications (optional).
 * @property location The geographical location of the user as a [GeoPoint] (optional).
 * @property geohash A geohash representation of the user's location (optional).
 * @property distanceToSelfProfile The distance of this profile from the user's current location, in
 *   meters (optional).
 * @property hasBlocked A list of user IDs that this user has blocked.
 * @property meetingRequestSent A list of user IDs to whom this user has sent meeting requests.
 * @property meetingRequestInbox A map containing meeting requests received, where the key is the
 *   sender's user ID and the value is the accompanying message.
 */
data class Profile(
    val uid: String,
    val name: String,
    val gender: Gender,
    val birthDate: Timestamp,
    val catchPhrase: String,
    val description: String,
    val tags: List<String> = listOf(),
    val profilePictureUrl: String? = null,
    val fcmToken: String? = null,
    val location: GeoPoint? = null,
    val geohash: String? = null,
    val distanceToSelfProfile: Int? = null,
    var hasBlocked: List<String> = listOf(),
    var hasAlreadyMet: List<String> = listOf(),
    var reports: Map<String, reportType> = mapOf(),
    val meetingRequestSent: List<String> = listOf(),
    val meetingRequestInbox: Map<String, String> = mapOf(),
    val meetingRequestPendingLocation: List<String> = listOf(),
    val meetingRequestChosenLocalisation: Map<String, Pair<Double, Double>> = mapOf()
) {
  /**
   * Calculates the user's age based on their birth date.
   *
   * @return The calculated age as an [Int].
   */
  fun calculateAge(): Int {
    val now = Calendar.getInstance()
    val birthDateCalendar = Calendar.getInstance().apply { time = birthDate.toDate() }
    var age = now.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < birthDateCalendar.get(Calendar.DAY_OF_YEAR)) {
      age--
    }
    return age
  }

  companion object
}

/**
 * Enum class representing gender options.
 *
 * @property displayName The display name for each gender.
 */
enum class Gender(val displayName: String) {
  WOMEN("Women"),
  MEN("Men"),
  OTHER("Other")
}

/**
 * Enum class representing reports types.
 *
 * @property displayName The display name for each report type.
 */
enum class reportType(val displayName: String) {
  INAPPROPRIATE_CONTENT("Inappropriate content"),
  SPAM("Spam"),
  OTHER("Other")
}
