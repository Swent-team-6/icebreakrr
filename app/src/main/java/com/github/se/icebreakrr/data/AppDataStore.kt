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

// File written with the help of CursorAI

/**
 * Manages persistent app preferences using DataStore. This class handles storage and retrieval of
 * user preferences such as:
 * - Authentication token
 * - Discoverability status
 *
 * All operations are suspending functions or return Flows to ensure safe background execution.
 */
open class AppDataStore(private val dataStore: DataStore<Preferences>) {
  companion object {
    private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "app_preferences")

    // Preference Keys using camelCase as per convention
    private val authTokenKey = stringPreferencesKey("auth_token")
    private val discoverableKey = booleanPreferencesKey("is_discoverable")

    // Default values
    const val DEFAULT_DISCOVERABLE_STATUS = true
  }

  /**
   * Secondary constructor that creates a DataStore instance from a Context.
   *
   * @param context The application context used to create the DataStore
   */
  constructor(context: Context) : this(context.dataStore)

  // Common preference Flows
  val authToken: Flow<String?> = dataStore.data.map { preferences -> preferences[authTokenKey] }
  val hasAuthToken: Flow<Boolean> = authToken.map { token -> !token.isNullOrEmpty() }
  val isDiscoverable: Flow<Boolean> = getPreference(discoverableKey, DEFAULT_DISCOVERABLE_STATUS)

  /**
   * Generic function to retrieve a Flow of a stored preference by key, with a default value.
   *
   * @param key Preference key to retrieve
   * @param defaultValue Default value if preference is not set
   * @return Flow of the preference value
   */
  private fun <T : Any?> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
      dataStore.data.map { preferences -> preferences[key] ?: defaultValue }

  /**
   * Generic function to store a preference with a given key and value.
   *
   * @param key Preference key to store
   * @param value Value to store
   */
  private suspend fun <T> putPreference(key: Preferences.Key<T>, value: T) {
    dataStore.edit { preferences -> preferences[key] = value }
  }

  /**
   * Generic function to remove a specific preference by its key.
   *
   * @param key Preference key to remove
   */
  private suspend fun <T> removePreference(key: Preferences.Key<T>) {
    dataStore.edit { preferences -> preferences.remove(key) }
  }

  /**
   * Saves an authentication token to persistent storage.
   *
   * @param token The authentication token to store
   * @throws IllegalArgumentException if token is empty or blank
   */
  suspend fun saveAuthToken(token: String) {
    require(token.isNotBlank()) { "Auth token cannot be empty or blank" }
    putPreference(authTokenKey, token)
  }

  /**
   * Removes the stored authentication token. Used during logout or when the token needs to be
   * invalidated.
   */
  suspend fun clearAuthToken() {
    removePreference(authTokenKey)
  }

  /** Save discoverability preference */
  suspend fun saveDiscoverableStatus(isDiscoverable: Boolean) {
    putPreference(discoverableKey, isDiscoverable)
  }

  /**
   * Clears all stored preferences. Use with caution as this will reset all preferences to their
   * default values.
   */
  suspend fun clearAll() {
    dataStore.edit { preferences -> preferences.clear() }
  }
}
