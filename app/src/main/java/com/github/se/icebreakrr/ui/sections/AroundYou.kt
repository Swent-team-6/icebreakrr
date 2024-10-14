package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.ui.Profile
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.shared.ProfileCard


/**
 * Composable function for displaying the "Around You" screen.
 *
 * It includes a bottom navigation bar and displays the main content of the screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AroundYouScreen(navigationActions: NavigationActions) {
    //todo: when viewmodel is implemented, replace this with viewmodel's data
    val twoProfiles = Profile.getMockedProfiles()
    val empty = emptyList<Profile>()

    val profiles = twoProfiles //todo: remove me after debugging


  Scaffold(
      modifier = Modifier.testTag("aroundYouScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = navigationActions.currentRoute())
      },

      topBar = {
          Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                  .background(Color(0xFF1FAEF0)) //fixme: use icebreakrr blue
                  .fillMaxWidth()
                  .heightIn(90.dp)
          ) {
              Text(
                  text = "Around You",
                  fontSize = 40.sp,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth()

              )
          }
      },
      content = { innerPadding ->
          if (profiles.isNotEmpty()) {
              LazyColumn(
                  contentPadding = PaddingValues(vertical = 16.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp),
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 8.dp)
                      .padding(innerPadding)){
                  items(profiles.size) { index ->
                      ProfileCard(
                          profile = profiles[index],
                          onclick = { /* todo: change selected profile in VM and navigate to viewprofile*/ }
                      )
                  }
              }
          } else {
              Box (
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                  Text(
                      modifier = Modifier.padding(innerPadding),
                      text = "There is no one around. Try moving!",
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF575757)
                  )
              }
          }
      })
}