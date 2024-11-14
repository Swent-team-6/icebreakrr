package com.github.se.icebreakrr.di.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Install in the application-wide Hilt component
object FirebaseAuthModule {
  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
  }
}

@Module
@InstallIn(SingletonComponent::class) // Install in the application-wide Hilt component
object FirestoreModule {
  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
  }
}
