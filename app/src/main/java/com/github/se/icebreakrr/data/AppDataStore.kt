package com.github.se.icebreakrr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This file was written with the help of CursorAI

/**
 * Manages persistent app preferences using DataStore. This class handles storage and retrieval of
 * user preferences such as:
 * - Authentication token
 * - Discoverability status
 *
 * All operations are suspending functions or return Flows to ensure safe background execution.
 */
open class AppDataStore(private val dataStore: DataStore<Preferences>) {
  /**
   * Secondary constructor that creates a DataStore instance from a Context.
   *
   * @param context The application context used to create the DataStore
   */
  constructor(context: Context) : this(context.dataStore)

  companion object {
    /**
     * DataStore instance created using the preferencesDataStore delegate. Ensures only one instance
     * of DataStore is created for the application.
     */
    private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "app_preferences")

    // Preference Keys
    /** Key for storing the authentication token Type: String */
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    /** Key for storing the user's discoverability preference Type: Boolean */
    private val DISCOVERABLE_KEY = booleanPreferencesKey("is_discoverable")
  }

  /**
   * Provides access to the stored authentication token as a Flow.
   *
   * @return Flow<String?> that emits the current auth token or null if not set
   */
  val authToken: Flow<String?> = dataStore.data.map { preferences -> preferences[AUTH_TOKEN_KEY] }

  /**
   * Saves an authentication token to persistent storage.
   *
   * @param token The authentication token to store
   */
  suspend fun saveAuthToken(token: String) {
    dataStore.edit { preferences -> preferences[AUTH_TOKEN_KEY] = token }
  }

  /**
   * Removes the stored authentication token. Used during logout or when the token needs to be
   * invalidated.
   */
  suspend fun clearAuthToken() {
    dataStore.edit { preferences -> preferences.remove(AUTH_TOKEN_KEY) }
  }

  /**
   * Clears all stored preferences. Use with caution as this will reset all preferences to their
   * default values.
   */
  suspend fun clearAll() {
    dataStore.edit { preferences -> preferences.clear() }
  }

  /**
   * Check if we have a valid auth token stored
   *
   * @return Flow<Boolean> indicating if we have a token
   */
  val hasAuthToken: Flow<Boolean> = authToken.map { token -> !token.isNullOrEmpty() }

  /** Save discoverability preference */
  suspend fun saveDiscoverableStatus(isDiscoverable: Boolean) {
    dataStore.edit { preferences -> preferences[DISCOVERABLE_KEY] = isDiscoverable }
  }

  /** Get discoverability status as a Flow Default value is true to maintain existing behavior */
  val isDiscoverable: Flow<Boolean> =
      dataStore.data.map { preferences -> preferences[DISCOVERABLE_KEY] ?: true }
}
