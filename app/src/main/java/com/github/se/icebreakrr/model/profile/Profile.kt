package com.github.se.icebreakrr.model.profile

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar

/**
 * Data class representing a user's profile.
 *
 * @property uid The unique identifier for the profile.
 * @property name The name of the user.
 * @property gender The user's gender.
 * @property birthDate The birth date of the user as a [Timestamp].
 * @property catchPhrase The user's catchphrase or tagline.
 * @property description A brief description of the user.
 * @property tags A list of tags associated with the user.
 * @property profilePictureUrl The URL of the user's profile picture (optional).
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
    var hasBlocked: List<String> = listOf(),
    val meetingRequestSent: List<String> = listOf(),
    val meetingRequestInbox: Map<String, String> = mapOf()
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
