package com.github.se.icebreakrr.model.tags

data class TagsCategory(
    val name: String = "",
    val color: String = "0xFFFFFFFF",
    val subtags: List<String> = emptyList()
)

enum class CategoryString {
  Sport,
  Music,
  Technology,
  Travel,
  Food,
  Art,
  Gaming,
  Cinema,
  Activism,
  Occupation,
  Philosophy
}
