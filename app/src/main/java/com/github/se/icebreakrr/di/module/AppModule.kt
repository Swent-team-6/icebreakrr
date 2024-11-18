package com.github.se.icebreakrr.di.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** FirebaseAuth object that Hilt will inject into the MainActivity when launching MainActivity */
@Module
@InstallIn(SingletonComponent::class) // Install in the application-wide Hilt component
object FirebaseAuthModule {
  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
  }
}

/**
 * FirebaseFirestore object that Hilt will inject into the MainActivity when launching MainActivity
 */
@Module
@InstallIn(SingletonComponent::class) // Install in the application-wide Hilt component
object FirestoreModule {
  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
  }
}

/**
 * AuthStateListener object that Hilt will inject into the MainActivity when launching MainActivity
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthStateListenerModule {
  @Provides
  @Singleton
  fun provideAuthStateListener(): AuthStateListener {
    return AuthStateListener {}
  }
}
