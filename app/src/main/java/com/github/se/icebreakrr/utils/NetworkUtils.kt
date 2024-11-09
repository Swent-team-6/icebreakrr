package com.github.se.icebreakrr.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.github.se.icebreakrr.R

// Written with the help of CursorAI and Claude

/**
 * A utility object for network-related functions.
 *
 * This object provides methods to check network availability and to show a toast message when there
 * is no internet connection.
 */
object NetworkUtils {

  /**
   * Checks if the network is available.
   *
   * @param context The context used to access system services.
   * @return True if the network is available, false otherwise.
   */
  fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    val currentNetwork = connectivityManager?.activeNetwork
    val caps = connectivityManager?.getNetworkCapabilities(currentNetwork)
    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
  }

  /**
   * Shows a toast message indicating that there is no internet connection.
   *
   * @param context The context used to display the toast message.
   */
  fun showNoInternetToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.No_Internet_Toast), Toast.LENGTH_LONG).show()
  }
}
