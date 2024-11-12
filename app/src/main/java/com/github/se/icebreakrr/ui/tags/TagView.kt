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
import com.github.se.icebreakrr.ui.theme.extendedTagsColor

// Define constants for commonly used dimensions and colors
private val TAG_PADDING = 8.dp
private val CLICKABLE_TAG_PADDING = 4.dp
private val TAG_CORNER_RADIUS = 16.dp
private val CLICKABLE_TAG_CORNER_RADIUS = 12.dp
private val DEFAULT_TAG_FONT_SIZE = 12.sp
private val DROP_DOWN_MENU_HEIGHT = 60.dp
private val DROP_DOWN_MENU_MAX_ITEMS = 5
private val TAG_BOX_PADDING = 8.dp
private val DROPDOWN_PADDING = 8.dp
private val SELECTED_TAG_COLOR = Color.Red
private val DEFAULT_TAG_BACKGROUND_COLOR = Color.Cyan
private val EXTEND_TAG_COLOR = extendedTagsColor

data class TagStyle(
    val textColor: Color = Color.Black,
    val backGroundColor: Color = DEFAULT_TAG_BACKGROUND_COLOR,
    val fontSize: TextUnit = DEFAULT_TAG_FONT_SIZE,
)

@Composable
fun Tag(text: String, tagStyle: TagStyle) {
  Box(
      modifier =
          Modifier.padding(TAG_PADDING)
              .background(
                  color = tagStyle.backGroundColor, shape = RoundedCornerShape(TAG_CORNER_RADIUS))
              .wrapContentSize()
              .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            text = "#$text",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.testTag("testTag"))
      }
}

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

@Composable
fun ExtendTag(tagStyle: TagStyle, onClick: () -> Unit) {
  Surface(
      modifier =
          Modifier.padding(CLICKABLE_TAG_PADDING)
              .clickable(onClick = onClick)
              .testTag("testExtendTag"),
      color = EXTEND_TAG_COLOR,
      shape = RoundedCornerShape(CLICKABLE_TAG_CORNER_RADIUS)) {
        Text(
            text = "...",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.padding(TAG_BOX_PADDING),
            textAlign = TextAlign.Center)
      }
}

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
            val tagsToShow = if (isExtended.value) tags else tags.take(3)
            tagsToShow.forEach { (text, color) ->
              Tag(text, TagStyle(tagStyle.textColor, color, tagStyle.fontSize))
            }
            if (!isExtended.value && tags.size > 3) {
              ExtendTag(tagStyle) { isExtended.value = true }
            }
          }
    }
  }
}

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
            val tagsToShow = if (isExtended.value) tags else tags.take(3)
            tagsToShow.forEach { (text, color) ->
              ClickTag(text, TagStyle(tagStyle.textColor, color, tagStyle.fontSize)) {
                onClick(text)
              }
            }
            if (!isExtended.value && tags.size > 3) {
              ExtendTag(tagStyle) { isExtended.value = true }
            }
          }
    }
  }
}

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
