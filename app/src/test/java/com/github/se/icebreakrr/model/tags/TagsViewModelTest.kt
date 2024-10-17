package com.github.se.icebreakrr.model.profile.tags

import androidx.compose.ui.graphics.Color
import com.github.se.icebreakrr.model.tags.TagsCategory
import com.github.se.icebreakrr.model.tags.TagsRepository
import com.github.se.icebreakrr.model.tags.TagsViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagsViewModelTest {
  private lateinit var repository: TagsRepository
  private lateinit var viewModel: TagsViewModel

  @Before
  fun setUp() {
    repository = mock(TagsRepository::class.java)
  }

  @Test
  fun GetAllTagsTest() {
    val fakeTags =
        listOf(
            TagsCategory("Category1", "0xFF0000", listOf("Tag1", "Tag2")),
            TagsCategory("Category2", "0x00FF00", listOf("Tag3", "Tag4")))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(fakeTags)
          null
        }
        .`when`(repository)
        .getAllTags(anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
          null
        }
        .`when`(repository)
        .init(any())

    viewModel = TagsViewModel(repository)
    assertEquals(fakeTags, viewModel.allTags)
  }

  @Test
  fun TagToColorTest() {
    val fakeTags = TagsCategory("Sport", "0xFF00FF00", subtags = listOf("Sport", "Football"))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(listOf(fakeTags))
          null
        }
        .`when`(repository)
        .getAllTags(any(), any())

    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
          null
        }
        .`when`(repository)
        .init(any())

    viewModel = TagsViewModel(repository)
    val color1 = viewModel.tagToColor("Football")
    assertEquals(
        Color(0xFF00FF00),
        color1,
    )

    val color2 = viewModel.tagToColor("Art")
    assertEquals(Color(0xFFFFFFFF), color2)
  }

  @Test
  fun SetQueryTest() {
    val fakeTags =
        listOf(
            TagsCategory(
                "Sport", "0xFF00FF00", subtags = listOf("Sport", "Football", "Basketball")),
            TagsCategory("Art", "0xFF00FF00", subtags = listOf("Art", "Photography", "Museum")))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(fakeTags)
          null
        }
        .`when`(repository)
        .getAllTags(anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
          null
        }
        .`when`(repository)
        .init(any())

    viewModel = TagsViewModel(repository)
    viewModel.setQuery("a", listOf("Basketball"))
    assertEquals(listOf("Football", "Art", "Photography"), viewModel.tagsSuggestions.value)
    assertEquals("a", viewModel.query.value)

    viewModel.leaveUI()
    assertEquals(emptyList<String>(), viewModel.tagsSuggestions.value)
    assertEquals("", viewModel.query.value)
  }

  @Test
  fun ApplyFilterTest() {
    val fakeTags =
        listOf(
            TagsCategory(
                "Sport", "0xFF00FF00", subtags = listOf("Sport", "Football", "Basketball")),
            TagsCategory("Art", "0xFF00FF00", subtags = listOf("Art", "Photography", "Museum")))

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<TagsCategory>) -> Unit>(0)
          onSuccess(fakeTags)
          null
        }
        .`when`(repository)
        .getAllTags(anyOrNull(), anyOrNull())
    doAnswer { invocation ->
          val callback = invocation.getArgument<() -> Unit>(0)
          callback.invoke()
          null
        }
        .`when`(repository)
        .init(any())

    viewModel = TagsViewModel(repository)
    viewModel.addFilter("Sport")
    viewModel.addFilter("Basketball")
    viewModel.addFilter("Art")
    viewModel.addFilter("BBQ Ribs")
    viewModel.addFilter("Philosophy")
    assertEquals(
        listOf("Sport", "Basketball", "Art", "BBQ Ribs", "Philosophy"),
        viewModel.filteringTags.value)
    viewModel.removeFilter("BBQ Ribs")
    viewModel.removeFilter("Philosophy")
    assertEquals(listOf("Sport", "Basketball", "Art"), viewModel.filteringTags.value)

    viewModel.applyFilters()
    assertEquals(listOf("Basketball", "Art", "Photography", "Museum"), viewModel.filteredTags.value)

    viewModel.removeFilter("Art")
    viewModel.removeFilter("Basketball")
    viewModel.removeFilter("Sport")
    viewModel.addFilter("Basketball")
    viewModel.addFilter("Sport")
    viewModel.addFilter("Art")
    assertEquals(listOf("Basketball", "Sport", "Art"), viewModel.filteringTags.value)

    viewModel.applyFilters()
    assertEquals(listOf("Basketball", "Art", "Photography", "Museum"), viewModel.filteredTags.value)
  }
}
