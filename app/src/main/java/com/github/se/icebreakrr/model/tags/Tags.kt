package com.github.se.icebreakrr.model.tags

data class TagsCategory(
    val uid: Long,
    val name: String,
    val color: String,
    val subtags: List<String>
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
