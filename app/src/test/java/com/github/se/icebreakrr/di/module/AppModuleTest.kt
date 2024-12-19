package com.github.se.icebreakrr.di.module

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppModuleTest {

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)
    }

    @Test
    fun testFirebaseAuthProvider() {
        val auth = FirebaseAuthModule.provideFirebaseAuth()
        assertNotNull(auth)
    }

    @Test
    fun testFirebaseFirestoreProvider() {
        val firestore = FirestoreModule.provideFirebaseFirestore()
        assertNotNull(firestore)
    }

    @Test
    fun testAuthStateListenerProvider(){
        val authState = AuthStateListenerModule.provideAuthStateListener()
        assertNotNull(authState)
    }
}