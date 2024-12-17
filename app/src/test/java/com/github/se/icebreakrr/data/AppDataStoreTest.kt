package com.github.se.icebreakrr.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// This file was written with the help of CursorAI
@OptIn(ExperimentalCoroutinesApi::class)
class AppDataStoreTest {
  private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

  @get:Rule val tempFolder: TemporaryFolder = TemporaryFolder()

  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var appDataStore: AppDataStore

  @Before
  fun setUp() {
    testDataStore =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tempFolder.newFolder(), "test_preferences.preferences_pb") })
    appDataStore = AppDataStore(testDataStore)
  }

  @Test
  fun testSaveAndRetrieveAuthToken() = runTest {
    val token = "test_token"
    appDataStore.saveAuthToken(token)

    val retrievedToken = appDataStore.authToken.first()
    assertEquals(token, retrievedToken)
  }

  @Test
  fun testClearAuthToken() = runTest {
    appDataStore.saveAuthToken("test_token")
    appDataStore.clearAuthToken()

    val retrievedToken = appDataStore.authToken.first()
    assertEquals(null, retrievedToken)
  }

  @Test
  fun testHasAuthToken() = runTest {
    appDataStore.saveAuthToken("test_token")
    val hasToken = appDataStore.hasAuthToken.first()
    assertEquals(true, hasToken)

    appDataStore.clearAuthToken()
    val hasNoToken = appDataStore.hasAuthToken.first()
    assertEquals(false, hasNoToken)
  }

  @Test
  fun testSaveAndRetrieveDiscoverableStatus() = runTest {
    appDataStore.saveDiscoverableStatus(false)
    val isDiscoverable = appDataStore.isDiscoverable.first()
    assertEquals(false, isDiscoverable)

    appDataStore.saveDiscoverableStatus(true)
    val isDiscoverableAgain = appDataStore.isDiscoverable.first()
    assertEquals(true, isDiscoverableAgain)
  }

  @Test
  fun testClearAll() = runTest {
    appDataStore.saveAuthToken("test_token")
    appDataStore.saveDiscoverableStatus(false)
    appDataStore.clearAll()

    val retrievedToken = appDataStore.authToken.first()
    val isDiscoverable = appDataStore.isDiscoverable.first()

    assertEquals(null, retrievedToken)
    assertEquals(true, isDiscoverable) // Default value is true
  }

  @Test
  fun testSaveAndRetrieveNotificationTime() = runTest {
    val uid = "test_user_123"
    val timestamp = System.currentTimeMillis()
    
    // Save notification time
    appDataStore.saveNotificationTime(uid, timestamp)
    
    // Retrieve and verify the saved time
    val retrievedTimes = appDataStore.lastNotificationTimes.first()
    assertEquals(timestamp, retrievedTimes[uid])
  }

  @Test
  fun testClearNotificationTime() = runTest {
    val uid = "test_user_123"
    val timestamp = System.currentTimeMillis()
    
    // First save a notification time
    appDataStore.saveNotificationTime(uid, timestamp)
    
    // Verify it was saved
    var retrievedTimes = appDataStore.lastNotificationTimes.first()
    assertEquals(timestamp, retrievedTimes[uid])
    
    // Clear the notification time
    appDataStore.clearNotificationTime(uid)
    
    // Verify it was cleared
    retrievedTimes = appDataStore.lastNotificationTimes.first()
    assertNull(retrievedTimes[uid])
  }

  @Test
  fun testMultipleNotificationTimes() = runTest {
    val uid1 = "user_1"
    val uid2 = "user_2"
    val timestamp1 = System.currentTimeMillis()
    val timestamp2 = timestamp1 + 1000 // 1 second later
    
    // Save notification times for multiple users
    appDataStore.saveNotificationTime(uid1, timestamp1)
    appDataStore.saveNotificationTime(uid2, timestamp2)
    
    // Retrieve and verify all times
    val retrievedTimes = appDataStore.lastNotificationTimes.first()
    assertEquals(2, retrievedTimes.size)
    assertEquals(timestamp1, retrievedTimes[uid1])
    assertEquals(timestamp2, retrievedTimes[uid2])
  }

  @Test
  fun testOverwriteNotificationTime() = runTest {
    val uid = "test_user_123"
    val timestamp1 = System.currentTimeMillis()
    val timestamp2 = timestamp1 + 1000 // 1 second later
    
    // Save initial notification time
    appDataStore.saveNotificationTime(uid, timestamp1)
    
    // Verify initial save
    var retrievedTimes = appDataStore.lastNotificationTimes.first()
    assertEquals(timestamp1, retrievedTimes[uid])
    
    // Overwrite with new timestamp
    appDataStore.saveNotificationTime(uid, timestamp2)
    
    // Verify overwrite
    retrievedTimes = appDataStore.lastNotificationTimes.first()
    assertEquals(timestamp2, retrievedTimes[uid])
  }
}
