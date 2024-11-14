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
}
