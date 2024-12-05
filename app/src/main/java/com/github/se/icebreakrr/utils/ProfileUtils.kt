package com.github.se.icebreakrr.utils

// Utility function to crop username
fun cropUsername(username: String, maxLength: Int): String {
  if (maxLength < 0) {
    return username
  }

  return if (username.length > maxLength) {
    username.take(maxLength) + "..."
  } else {
    username
  }
}
