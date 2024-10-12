package com.github.se.icebreakrr.ui.tags

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.github.se.icebreakrr.model.tags.TagsViewModel

/**
 * Creates a small tag with a chosen color and a given text
 * @param s : The text we want included in the tag
 * @param color : The color of our tag
 */
@Composable
fun Tag(s: String, color: Color) {
  Box(
      modifier =
          Modifier.padding(8.dp)
              .background(
                  color = color,
                  shape = RoundedCornerShape(16.dp)
                  )
              .wrapContentSize()
              .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            text = "#$s",
            color = Color(0xFF4A4A4A), // Dark gray text color
            fontSize = 16.sp // Font size
            )
      }
}

/**
 * Creates a small tag with a chosen color, a given text, and a action on click event
 * @param s : The text we want included in the tag
 * @param color : The color of our tag
 * @param onClick : The on click event
 */
@Composable
fun ClickTag(s: String, color: Color, onClick: () -> Unit){
    Box(
        modifier =
        Modifier.padding(8.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
            .wrapContentSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Button(
            onClick =  onClick,
            colors = ButtonColors(color, color, color, color)
            ) {
            Text(text = "#$s",
                color = Color(0xFF4A4A4A), // Dark gray text color
                fontSize = 16.sp // Font size
            )
        }
    }
}

/**
 * This Composable is used in the Edit Profile, allows a user to enter a text in a text field ant to get a list of tags to choose from
 * We also get a collection of all the tags we have already selected
 * @param ProfileTag : all the tags in the ProfileViewModel of a user
 * @param tagsViewModel : view model of the tags
 * @param stringQuery : The text that is modified by the user
 * @param expanded : Choose if the drop down menu is activated or not
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    ProfileTag: MutableState<List<Pair<String, Color>>>,
    tagsViewModel: TagsViewModel,
    stringQuery: MutableState<String>,
    expanded: MutableState<Boolean>,
    onTagClick: (String) -> Unit
) {

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = stringQuery.value,
        onValueChange = {
          stringQuery.value = it
          tagsViewModel.getTagsWithName(stringQuery.value)
          expanded.value = true
        },
        label = { Text("Tags") },
        placeholder = { Text("Search for tags") },
    )
    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
        modifier = Modifier.fillMaxWidth(),
        properties = PopupProperties(focusable = false)) {
          val tagsList = tagsViewModel.outputTags.collectAsState().value
          tagsList.forEach { tag ->
            DropdownMenuItem(
                onClick = {
                  stringQuery.value = ""
                  expanded.value = false
                  Log.d("TAG CHOSEN", tag.first)
                },
                text = { Tag(tag.first, tag.second) },
            )
          }
        }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val profileList = ProfileTag.value
            FlowRow(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top
            ) {
                profileList.forEach { pair -> ClickTag(pair.first, pair.second) { onTagClick(pair.first) } }
            }
        }
    }
  }
}
