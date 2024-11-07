package com.github.se.icebreakrr.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

// Written with the help of CursorAI and Claude
object NetworkUtils {
  fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    val currentNetwork = connectivityManager.activeNetwork
    val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
  }

  fun showNoInternetToast(context: Context) {
    Toast.makeText(context, "No internet connection available", Toast.LENGTH_LONG).show()
  }
}
