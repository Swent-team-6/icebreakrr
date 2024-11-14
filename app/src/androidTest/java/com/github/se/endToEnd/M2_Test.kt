package com.github.se.endToEnd

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import com.github.se.icebreakrr.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.hamcrest.Matcher
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.functions.dagger.Provides
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.junit.After
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.mockingDetails
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class M2Test {
    val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
    @Inject lateinit var authInjected: FirebaseAuth
    @Inject lateinit var firestoreInjected: FirebaseFirestore

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup(){
        hiltRule.inject()
        intent.putExtra("IS_TESTING", true)
        ActivityCompat.setPermissionCompatDelegate(TestPermissionDelegate())
    }

    @Test
    fun endToEnd2(){
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            onIdle()

            // Check if the "Settings" button is displayed and perform a click
            composeTestRule.onNodeWithTag("aroundYouScreen")
                .isDisplayed()
            composeTestRule.onNodeWithTag("navItem_2131755372").isDisplayed()
            composeTestRule.onNodeWithTag("navItem_2131755372")
                .performClick()
            onIdle()
            Thread.sleep(2000)
        }
    }

    fun logViewHierarchy(viewMatcher: Matcher<View>) {
        onView(viewMatcher).check { view, _ ->
            Log.d("ViewHierarchyBite", HumanReadables.describe(view))
        }
    }
}
class TestPermissionDelegate : ActivityCompat.PermissionCompatDelegate {

    // Simulate the permission request being granted without showing the actual permission dialog
    override fun requestPermissions(
        activity: Activity,
        permissions: Array<out String>,
        requestCode: Int
    ): Boolean {
        // Directly simulate granting the permission
        return true
    }

    // Handle activity results (return true as a default response)
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        // Return true to indicate the activity result was successfully handled
        return true
    }
}







