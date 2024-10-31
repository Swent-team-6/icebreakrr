package com.github.se.icebreakrr.model.message

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MeetingRequestService : FirebaseMessagingService() {
  //  private val userAuth: FirebaseAuth
  //  private val profileViewModel: ProfilesViewModel

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    // Handle message data payload
    remoteMessage.data?.let {
      val message = it["message"]
      val senderId = it["senderId"]
    }
    // ToDo : make the message accessible outside of the class, so that the Notification screen can
    // get it
  }

  override fun onNewToken(token: String) {
    //    userAuth.currentUser?.let {
    //      profilesViewModel.getProfileByUid(it.uid)
    //      val userProfile = profilesViewModel.selectedProfile.value
    //      if (userProfile != null) {
    //        val profileWithNewToken = userProfile.copy(fcmToken = token)
    //        profilesViewModel.updateProfile(profileWithNewToken)
    //      }
    //    }
    super.onNewToken(token)
  }

  suspend fun sendMessageToUser(targetToken: String, message: String, senderId: String) {
    // ToDo: Set up a Kotlin Server to deal with the message sending system (can't be done directly
    // here)
  }
}
