package com.github.se.icebreakrr.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

// Define constants for commonly used dimensions and colors
private val TAG_PADDING = 8.dp
private val CLICKABLE_TAG_PADDING = 4.dp
private val TAG_CORNER_RADIUS = 16.dp
private val CLICKABLE_TAG_CORNER_RADIUS = 12.dp
private val DEFAULT_TAG_FONT_SIZE = 12.sp
private val DROP_DOWN_MENU_HEIGHT = 60.dp
private val DROP_DOWN_MENU_MAX_ITEMS = 5
private val TAG_BOX_PADDING = 8.dp
private val SELECTED_TAG_COLOR = Color.Red
private val DEFAULT_TAG_BACKGROUND_COLOR = Color.Cyan
private val TAG_HORIZONTAL_PADDING = 12.dp
private val TAG_VERTICAL_PADDING = 6.dp

private val TAGS_SHOWN_DEFAULT = 3

data class TagStyle(
    val textColor: Color = Color.Black,
    val backGroundColor: Color = DEFAULT_TAG_BACKGROUND_COLOR,
    val fontSize: TextUnit = DEFAULT_TAG_FONT_SIZE,
)

/**
 * Creates a small tag with a chosen color and a given text
 *
 * @param text : The text we want included in the tag
 * @param tagStyle : The style of the tag
 */
@Composable
fun Tag(text: String, tagStyle: TagStyle) {
  Box(
      modifier =
          Modifier.padding(TAG_PADDING)
              .background(
                  color = tagStyle.backGroundColor, shape = RoundedCornerShape(TAG_CORNER_RADIUS))
              .wrapContentSize()
              .padding(horizontal = TAG_HORIZONTAL_PADDING, vertical = TAG_VERTICAL_PADDING)) {
        Text(
            text = "#$text",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.testTag("testTag"))
      }
}

/**
 * Creates a small tag with a chosen color, a given text, and a action on click event
 *
 * @param text : The text we want included in the tag
 * @param tagStyle : The style of the tag
 * @param onClick : The on click event
 */
@Composable
fun ClickTag(text: String, tagStyle: TagStyle, onClick: () -> Unit) {
  Surface(
      modifier =
          Modifier.padding(CLICKABLE_TAG_PADDING)
              .clickable(onClick = onClick)
              .testTag("clickTestTag"),
      color = tagStyle.backGroundColor,
      shape = RoundedCornerShape(CLICKABLE_TAG_CORNER_RADIUS)) {
        Text(
            text = "#$text x",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.padding(TAG_BOX_PADDING),
            textAlign = TextAlign.Center)
      }
}

/**
 * A tag that is used to hide a large list of tags
 *
 * @param tagStyle: the style of the tag
 * @param onClick: the click event when the user clicks on the tag
 */
@Composable
fun ExtendTag(tagStyle: TagStyle, onClick: () -> Unit) {
  Surface(
      modifier =
          Modifier.padding(CLICKABLE_TAG_PADDING)
              .clickable(onClick = onClick)
              .testTag("testExtendTag"),
      color = MaterialTheme.colorScheme.onPrimary,
      shape = RoundedCornerShape(CLICKABLE_TAG_CORNER_RADIUS)) {
        Text(
            text = "...",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.padding(TAG_BOX_PADDING),
            textAlign = TextAlign.Center)
      }
}

/**
 * This Composable is a row of tags that are that adapts dynamically to the space it has available
 *
 * @param tags: The list of tags to show
 * @param tagStyle: the style of your tags
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowOfTags(tags: List<Pair<String, Color>>, tagStyle: TagStyle) {
  val isExtended = remember { mutableStateOf(false) }
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item {
      FlowRow(
          modifier = Modifier.padding(TAG_PADDING),
          horizontalArrangement = Arrangement.Start,
          verticalArrangement = Arrangement.Top) {
            val tagsToShow = if (isExtended.value) tags else tags.take(TAGS_SHOWN_DEFAULT)
            tagsToShow.forEach { (text, color) ->
              Tag(text, TagStyle(tagStyle.textColor, color, tagStyle.fontSize))
            }
            if (!isExtended.value && tags.size > TAGS_SHOWN_DEFAULT) {
              ExtendTag(tagStyle) { isExtended.value = true }
            }
          }
    }
  }
}

/**
 * This Composable is a row of tags that are clickable and that adapts dynamically to the space it
 * has available
 *
 * @param tags: The list of tags to show
 * @param tagStyle: the style of your tags
 * @param onClick: the event when the user clicks on a tag
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowOfClickTags(tags: List<Pair<String, Color>>, tagStyle: TagStyle, onClick: (String) -> Unit) {
  val isExtended = remember { mutableStateOf(false) }
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item {
      FlowRow(
          modifier = Modifier.padding(TAG_PADDING),
          horizontalArrangement = Arrangement.Start,
          verticalArrangement = Arrangement.Top) {
            val tagsToShow = if (isExtended.value) tags else tags.take(TAGS_SHOWN_DEFAULT)
            tagsToShow.forEach { (text, color) ->
              ClickTag(text, TagStyle(tagStyle.textColor, color, tagStyle.fontSize)) {
                onClick(text)
              }
            }
            if (!isExtended.value && tags.size > TAGS_SHOWN_DEFAULT) {
              ExtendTag(tagStyle) { isExtended.value = true }
            }
          }
    }
  }
}

/**
 * This Composable is used in the Edit Profile, allows a user to enter a text in a text field ant to
 * get a list of tags to choose from We also get a collection of all the tags we have already
 * selected
 *
 * @param selectedTag : all the tags in the that have already been selected
 * @param outputTag : the tags that drop down in the DropDownMenu
 * @param stringQuery : The text that is modified by the user
 * @param expanded : Choose if the drop down menu is activated or not
 * @param onTagClick : The event happening when the user clicks on a tag
 * @param onDropDownItemClicked : The event when the user clicks on a DropDownMenuItem
 * @param onStringChanged : The event when the string is changed by the user
 * @param textColor : The color of the text we want in the tags selected and on the tags in the drop
 *   down menu
 * @param textSize : The size of the text in the tags selected and on the tags in the drop down menu
 *   The color in the tag depends on the category of the tag
 */
@Composable
fun TagSelector(
    selectedTag: List<Pair<String, Color>>,
    outputTag: List<Pair<String, Color>>,
    stringQuery: MutableState<String>,
    expanded: MutableState<Boolean>,
    onTagClick: (String) -> Unit,
    onDropDownItemClicked: (String) -> Unit,
    onStringChanged: (String) -> Unit,
    textColor: Color,
    textSize: TextUnit,
    width: Dp,
    height: Dp
) {
  val dropdownMenuHeight =
      (outputTag.size.coerceAtMost(DROP_DOWN_MENU_MAX_ITEMS) * DROP_DOWN_MENU_HEIGHT.value).dp

  Box(modifier = Modifier.size(width, height).testTag("sizeTagSelector")) {
    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
      Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        DropdownMenu(
            expanded = expanded.value && dropdownMenuHeight != 0.dp,
            onDismissRequest = { expanded.value = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.height(dropdownMenuHeight)) {
              outputTag.forEach { (text, color) ->
                DropdownMenuItem(
                    onClick = {
                      expanded.value = false
                      onDropDownItemClicked(text)
                      stringQuery.value = ""
                    },
                    text = { Tag(text, TagStyle(textColor, color, textSize)) },
                    modifier = Modifier.testTag("tagSelectorDropDownMenuItem"))
              }
            }
        OutlinedTextField(
            value = stringQuery.value,
            onValueChange = {
              onStringChanged(it)
              expanded.value = true
            },
            label = { Text("Tags", modifier = Modifier.testTag("labelTagSelector")) },
            placeholder = {
              Text("Search for tags", modifier = Modifier.testTag("placeholderTagSelector"))
            },
            modifier = Modifier.testTag("inputTagSelector"))
      }
      RowOfClickTags(selectedTag, TagStyle(textColor, SELECTED_TAG_COLOR, textSize)) {
        onTagClick(it)
      }
    }
  }
}
