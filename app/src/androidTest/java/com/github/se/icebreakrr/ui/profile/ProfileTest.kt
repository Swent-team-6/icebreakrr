import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.icebreakrr.ui.navigation.NavigationActions
import com.github.se.icebreakrr.ui.profile.ProfileView
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ProfileViewTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActionsMock: NavigationActions

  @Before
  fun setUp() {
    navigationActionsMock = mock()
  }

  @Test
  fun testProfileHeaderDisplaysCorrectly() {
    composeTestRule.setContent { ProfileView(navigationActions = navigationActionsMock) }

    // Verify that the profile header and its elements are displayed
    composeTestRule.onNodeWithTag("profileHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username").assertTextEquals("Jean Michelle")
  }

  @Test
  fun testGoBackButtonFunctionality() {
    composeTestRule.setContent { ProfileView(navigationActions = navigationActionsMock) }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    verify(navigationActionsMock).goBack()
  }

  @Test
  fun testEditButtonFunctionality() {
    composeTestRule.setContent { ProfileView(navigationActions = navigationActionsMock) }

    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").performClick()

    // Add edit profile navigation logic
  }

  @Test
  fun testInfoSectionDisplaysCorrectly() {
    composeTestRule.setContent { ProfileView(navigationActions = navigationActionsMock) }

    // Verify that the info section is displayed
    composeTestRule.onNodeWithTag("infoSection").assertIsDisplayed()

    // Verify that the catchphrase is displayed
    composeTestRule
        .onNodeWithText("This is my catchphrase, there are many like it but this one is mine!")
        .assertIsDisplayed()

    // Verify that all tags are displayed
    composeTestRule.onNodeWithTag("tagSection").assertIsDisplayed()

    // Verify that the description is displayed
    composeTestRule.onNodeWithTag("profileDescription").assertIsDisplayed()
  }

  @Test
  fun testProfileDescriptionDisplaysCorrectly() {
    composeTestRule.setContent { ProfileView(navigationActions = navigationActionsMock) }

    // Verify that the profile description is displayed correctly
    composeTestRule
        .onNodeWithTag("profileDescription")
        .assertTextContains(
            """
                Hey there! I'm John Do, a bit of a tech geek who loves exploring all things digital. 
                When I'm not deep into coding or working on new apps, I'm usually out cycling, discovering new trails, 
                or whipping up something fun in the kitchen. I'm also a huge sci-fi fan—if you love talking about future 
                worlds or cool ideas, we'll get along great. I'm always up for a good laugh, sharing ideas, and meeting new people. 
                Let's connect, hang out, or chat about anything cool!
            """
                .trimIndent())
        .assertIsDisplayed()
  }
}
