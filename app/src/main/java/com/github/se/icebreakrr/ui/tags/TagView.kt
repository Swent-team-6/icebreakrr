package com.github.se.icebreakrr.ui.tags

import android.util.Log
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
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
            text = "#$s",
            color = tagStyle.textColor,
            fontSize = tagStyle.fontSize,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
      }
}

/**
 * This Composable is used in the Edit Profile, allows a user to enter a text in a text field ant to
 * get a list of tags to choose from We also get a collection of all the tags we have already
 * selected
 *
 * @param profileTag : all the tags in the ProfileViewModel of a user
 * @param outputTag : The tags gotten with the TagsViewModel when searching with the stringQuery
 * @param stringQuery : The text that is modified by the user
 * @param expanded : Choose if the drop down menu is activated or not
 * @param onTagClick : The event happening when the user clicks on a tag
 * @param onStringChanged : The event when the string is changed by the user
 * @param textColor : The color of the text we want in the tags selected and on the tags in the drop
 *   down menu
 * @param textSize : The size of the text in the tags selected and on the tags in the drop down menu
 *   The color in the tag depends on the category of the tag
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    profileTag: MutableState<List<Pair<String, Color>>>,
    outputTag: MutableState<List<Pair<String, Color>>>,
    stringQuery: MutableState<String>,
    expanded: MutableState<Boolean>,
    onTagClick: (String) -> Unit,
    onStringChanged: (String) -> Unit,
    textColor: Color,
    textSize: TextUnit
) {

  Column(modifier = Modifier.fillMaxWidth()) {
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
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
        modifier = Modifier.fillMaxWidth(),
        properties = PopupProperties(focusable = false)) {
          val tagsList = outputTag.value
          tagsList.forEach { tag ->
            DropdownMenuItem(
                onClick = {
                  stringQuery.value = ""
                  expanded.value = false
                  Log.d("TAG CHOSEN", tag.first)
                },
                text = { Tag(tag.first, TagStyle(textColor, tag.second, textSize)) },
            )
          }
        }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      item {
        val profileList = profileTag.value
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top) {
              profileList.forEach { pair ->
                ClickTag(pair.first, TagStyle(textColor, pair.second, textSize)) {
                  onTagClick(pair.first)
                }
              }
            }
      }
    }
  }
}
