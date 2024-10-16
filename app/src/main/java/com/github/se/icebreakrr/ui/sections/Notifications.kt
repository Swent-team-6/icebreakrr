package com.github.se.icebreakrr.ui.sections

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.icebreakrr.ui.navigation.BottomNavigationMenu
import com.github.se.icebreakrr.ui.navigation.LIST_TOP_LEVEL_DESTINATIONS
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.navigation.Route
import com.github.se.icebreakrr.ui.navigation.Screen
import com.github.se.icebreakrr.ui.theme.FilterIcon
import com.github.se.icebreakrr.ui.theme.IceBreakrrBlue
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Composable function for displaying the notification screen.
 *
 * It includes a bottom navigation bar and displays the main content of the notification screen.
 *
 * @param navigationActions The actions used for navigating between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(navigationActions: NavigationActions) {
  val cardList = remember {
    mutableStateOf(
        listOf(
            "Michelle",
            "Paul",
            "Francois",
            "Jean",
            "Van Dam",
            "Schwartzy",
            "Otto",
            "Gunter",
            "Stuart"))
  }
  Scaffold(
      modifier = Modifier.testTag("notificationScreen"),
      topBar = { TopAppBarNotification() },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATIONS,
            selectedItem = Route.NOTIFICATIONS)
      },
      content = { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().testTag("notificationScroll")) {
              item {
                Text(
                    text = "Pending meeting requests",
                    modifier = Modifier.padding(innerPadding).testTag("notificationFirstText"))
                Column { cardList.value.forEach { s -> Text(s) } }
                Text(
                    text = "Passed",
                    modifier = Modifier.padding(innerPadding).testTag("notificationSecondText"))
                Column { cardList.value.forEach { s -> Text(s) } }
              }
            }
      },
      floatingActionButton = {FilterFloatingActionButton(navigationActions)},
  )
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarNotification() {
  Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().height(101.dp).padding(bottom = 2.dp)) {
        TopAppBar(
            title = {
              Text(
                  modifier =
                      Modifier.fillMaxSize()
                          .align(Alignment.CenterVertically)
                          .padding(25.dp)
                          .testTag("notificationTopAppBar"),
                  text = "Inbox",
                  style =
                      TextStyle(
                          fontSize = 40.sp,
                          fontFamily = FontFamily.Default,
                          fontWeight = FontWeight(700),
                          color = Color(0xFFFFFFFF),
                          textAlign = TextAlign.Center))
            },
            colors =
                TopAppBarColors(IceBreakrrBlue, Color.Blue, Color.Blue, Color.White, Color.White),
            modifier = Modifier.fillMaxSize().align(Alignment.CenterVertically))
      }
}

@Composable
fun FilterFloatingActionButton(navigationActions : NavigationActions){
    FloatingActionButton(
        onClick = { navigationActions.navigateTo(Screen.FILTER) },
        modifier = Modifier.testTag("filterButton"),
        containerColor = IceBreakrrBlue,
        contentColor = Color.White) {
        Icon(imageVector = FilterIcon, contentDescription = "Filter")
    }
}
