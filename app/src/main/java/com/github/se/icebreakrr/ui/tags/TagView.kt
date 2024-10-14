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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

data class TagStyle(
    val textColor: Color = Color.Black,
    val backGroundColor: Color = Color.Cyan,
    val fontSize: TextUnit = 12.sp,
)

/**
 * Creates a small tag with a chosen color and a given text
 *
 * @param s : The text we want included in the tag
 * @param tagStyle : The style of the tag
 */
@Composable
fun Tag(s: String, tagStyle: TagStyle) {
  Box(
      modifier =
          Modifier.padding(8.dp)
              .background(color = tagStyle.backGroundColor, shape = RoundedCornerShape(16.dp))
              .wrapContentSize()
              .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            text = "#$s",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.testTag("testTag"))
      }
}

/**
 * Creates a small tag with a chosen color, a given text, and a action on click event
 *
 * @param s : The text we want included in the tag
 * @param tagStyle : The style of the tag
 * @param onClick : The on click event
 */
@Composable
fun ClickTag(s: String, tagStyle: TagStyle, onClick: () -> Unit) {
  Surface(
      modifier = Modifier.padding(4.dp).clickable(onClick = onClick).testTag("clickTestTag"),
      color = tagStyle.backGroundColor,
      shape = RoundedCornerShape(12.dp)) {
        Text(
            text = "#$s x",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
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
      modifier = Modifier.padding(4.dp).clickable(onClick = onClick).testTag("testExtendTag"),
      color = Color(red = 220, green = 220, blue = 220),
      shape = RoundedCornerShape(12.dp)) {
        Text(
            text = "...",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
      }
}

/**
 * This Composable is a row of tags that are that adapts dynamically to the space it has available
 *
 * @param l: The list of tags to show
 * @param tagStyle: the style of your tags
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowOfTags(l: List<Pair<String, Color>>, tagStyle: TagStyle) {
  val isExtended = remember { mutableStateOf(false) }
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item {
      FlowRow(
          modifier = Modifier.padding(8.dp),
          horizontalArrangement = Arrangement.Start,
          verticalArrangement = Arrangement.Top) {
            if (isExtended.value) {
              l.forEach { pair ->
                Tag(pair.first, TagStyle(tagStyle.textColor, pair.second, tagStyle.fontSize))
              }
            } else {
              val notExtendedList = l.take(3)
              notExtendedList.forEach { pair ->
                Tag(pair.first, TagStyle(tagStyle.textColor, pair.second, tagStyle.fontSize))
              }
              if (notExtendedList.size >= 3) {
                ExtendTag(tagStyle) { isExtended.value = true }
              }
            }
          }
    }
  }
}

/**
 * This Composable is a row of tags that are clickable and that adapts dynamically to the space it
 * has available
 *
 * @param l: The list of tags to show
 * @param tagStyle: the style of your tags
 * @param onClick: the event when the user clicks on a tag
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowOfClickTags(l: List<Pair<String, Color>>, tagStyle: TagStyle, onClick: (String) -> Unit) {
  val isExtended = remember { mutableStateOf(false) }
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item {
      FlowRow(
          modifier = Modifier.padding(8.dp),
          horizontalArrangement = Arrangement.Start,
          verticalArrangement = Arrangement.Top) {
            if (isExtended.value) {
              l.forEach { pair ->
                ClickTag(pair.first, TagStyle(tagStyle.textColor, pair.second, tagStyle.fontSize)) {
                  onClick(pair.first)
                }
              }
            } else {
              val notExtendedList = l.take(3)
              notExtendedList.forEach { pair ->
                ClickTag(pair.first, TagStyle(tagStyle.textColor, pair.second, tagStyle.fontSize)) {
                  onClick(pair.first)
                }
              }
              if (notExtendedList.size >= 3) {
                ExtendTag(tagStyle) { isExtended.value = true }
              }
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
    selectedTag: MutableState<List<Pair<String, Color>>>,
    outputTag: MutableState<List<Pair<String, Color>>>,
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

    Box (
        modifier = Modifier.size(width, height).testTag("sizeTagSelector")
    ) {
        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
          OutlinedTextField(
              value = stringQuery.value,
              onValueChange = {
                stringQuery.value = it
                onStringChanged(stringQuery.value)
                expanded.value = true
              },
              label = { Text("Tags") },
              placeholder = { Text("Search for tags") },
              modifier = Modifier.testTag("inputTagSelector"))

          Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.align(Alignment.TopStart)) {
                  val tagsList = outputTag.value
                  tagsList.forEach { tag ->
                    DropdownMenuItem(
                        onClick = {
                          stringQuery.value = ""
                          expanded.value = false
                          onDropDownItemClicked(tag.first)
                        },
                        text = { Tag(tag.first, TagStyle(textColor, tag.second, textSize)) },
                        modifier = Modifier.testTag("tagSelectorDropDownMenuItem"))
                  }
                }
          }
          RowOfClickTags(selectedTag.value, TagStyle(textColor, Color.Red, textSize)) { s ->
            onTagClick(s)
          }
        }
    }
}
