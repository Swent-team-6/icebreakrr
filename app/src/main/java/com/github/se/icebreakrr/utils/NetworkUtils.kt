package com.github.se.icebreakrr.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

// This File was written with the help of Cursor AI

/**
 * Utility object for handling network-related operations and checks.
 * Provides functionality to:
 * - Check network connectivity
 * - Display network status messages
 * - Initialize network monitoring
 */
object NetworkUtils {
  /**
   * ConnectivityManager instance used to monitor network state.
   * Initialized via [init] or [setConnectivityManagerForTesting].
   */
  private var connectivityManager: ConnectivityManager? = null

  /**
   * Initializes the NetworkUtils with the application context.
   * Must be called before using other methods in this class.
   *
   * @param context The application context used to get the ConnectivityManager service
   */
  fun init(context: Context) {
    connectivityManager = context.getSystemService(ConnectivityManager::class.java)
  }

  /**
   * Sets a custom ConnectivityManager instance for testing purposes.
   * This allows mocking of network states in unit tests.
   *
   * @param manager The mock ConnectivityManager instance to use for testing
   */
  fun setConnectivityManagerForTesting(manager: ConnectivityManager) {
    connectivityManager = manager
  }

  /**
   * Checks if the device currently has a valid internet connection.
   * Uses NET_CAPABILITY_VALIDATED to ensure the network can reach the internet.
   *
   * @return true if internet is available, false otherwise
   */
  fun isNetworkAvailable(): Boolean {
    val currentNetwork = connectivityManager?.activeNetwork
    val caps = connectivityManager?.getNetworkCapabilities(currentNetwork)
    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
  }

  /**
   * Displays a toast message indicating no internet connection is available.
   *
   * @param context The context used to show the toast message
   */
  fun showNoInternetToast(context: Context) {
    Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
  }
}
