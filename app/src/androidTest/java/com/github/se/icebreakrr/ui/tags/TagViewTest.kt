//package com.github.se.icebreakrr.ui.tags
//
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.assertTextEquals
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.performClick
//import androidx.compose.ui.test.performTextClearance
//import androidx.compose.ui.test.performTextInput
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.mockito.Mockito.mock
//import org.mockito.Mockito.`when`
//import org.mockito.kotlin.any
//import org.mockito.kotlin.never
//import org.mockito.kotlin.verify
//
//class AddToDoScreenTest {
//    private lateinit var toDosRepository: ToDosRepository
//    private lateinit var navigationActions: NavigationActions
//    private lateinit var listToDosViewModel: ListToDosViewModel
//
//    @get:Rule val composeTestRule = createComposeRule()
//
//    @Before
//    fun setUp() {
//        // Mock is a way to create a fake object that can be used in place of a real object
//        toDosRepository = mock(ToDosRepository::class.java)
//        navigationActions = mock(NavigationActions::class.java)
//        listToDosViewModel = ListToDosViewModel(toDosRepository)
//
//        // Mock the current route to be the add todo screen
//        `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_TODO)
//    }
//
//    @Test
//    fun displayAllComponents() {
//        composeTestRule.setContent { AddToDoScreen(listToDosViewModel, navigationActions) }
//
//        composeTestRule.onNodeWithTag("addScreen").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("addTodoTitle").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("addTodoTitle").assertTextEquals("Create a new task")
//        composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("todoSave").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("todoSave").assertTextEquals("Save")
//
//        composeTestRule.onNodeWithTag("inputTodoTitle").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("inputTodoDescription").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("inputTodoAssignee").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("inputTodoLocation").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("inputTodoDate").assertIsDisplayed()
//    }
//
//    @Test
//    fun doesNotSubmitWithInvalidDate() {
//        composeTestRule.setContent { AddToDoScreen(listToDosViewModel, navigationActions) }
//
//        composeTestRule.onNodeWithTag("inputTodoDate").performTextClearance()
//        composeTestRule.onNodeWithTag("inputTodoDate").performTextInput("notadate")
//        composeTestRule.onNodeWithTag("todoSave").performClick()
//
//        verify(toDosRepository, never()).updateToDo(any(), any(), any())
//    }
//}
