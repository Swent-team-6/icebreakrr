package com.github.se.icebreakrr.model.tags

data class TagsCategory(
    val uid: Long,
    val name: String,
    val color: String,
    val subtags: List<String>
)
