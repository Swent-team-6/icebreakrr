package com.github.se.icebreakrr.utils

import android.util.Log
import com.github.se.icebreakrr.model.profile.Gender
import com.github.se.icebreakrr.model.profile.Profile
import com.github.se.icebreakrr.model.profile.ProfilesRepository
import com.github.se.icebreakrr.model.profile.ProfilesViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlin.random.Random

// Predefined user IDs
val predefinedUserIds = listOf(
    "usertest1", "usertest2", "usertest3", "usertest4", 
    "usertest5", "usertest6", "usertest7", "usertest8", 
    "usertest9", "usertest10", "usertest11", "usertest12", 
    "usertest13", "usertest14", "usertest15", "usertest16",
    "usertest17", "usertest18", "usertest19", "usertest20",
    "usertest21", "usertest22", "usertest23", "usertest24",
    "usertest25"
)

// Function to generate fake users around a specific location and add them to the database
fun generateFakeUsers(centerLat: Double, centerLon: Double, radius: Double, profilesViewModel: ProfilesViewModel) {
    for (userId in predefinedUserIds) {

        // Generate random latitude and longitude within the specified radius
        val randomLat = centerLat + (Random.nextDouble() - 0.5) * (radius / 50000) // Adjust radius for sparsity
        val randomLon = centerLon + (Random.nextDouble() - 0.5) * (radius / (50000 * Math.cos(Math.toRadians(centerLat)))) // Adjust for latitude

        // Generate geohash for the random location
        val geohash = GeoHashUtils.encode(randomLat, randomLon, precision = 7)

        // Create a fake profile
        val fakeProfile = Profile(
            uid = userId,
            name = "Fake User $userId",
            gender = if (Random.nextBoolean()) Gender.MEN else Gender.WOMEN,
            birthDate = Timestamp.now(),
            catchPhrase = "Hello, I'm $userId!",
            description = "This is a fake profile for user $userId.",
            tags = listOf("fake", "user", "test"),
            location = GeoPoint(randomLat, randomLon),
            geohash = geohash
        )

        // Add the fake profile to the repository
        profilesViewModel.addNewProfile(fakeProfile)

    }
}

// Function to delete fake users by their IDs
fun deleteFakeUsers(profilesViewModel: ProfilesViewModel) {

    for (userId in predefinedUserIds) {
        profilesViewModel.deleteProfileByUid(userId)
    }

}