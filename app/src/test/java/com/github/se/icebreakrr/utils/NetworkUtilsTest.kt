package com.github.se.icebreakrr.utils

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class NetworkUtilsTest {

  private lateinit var connectivityManager: ConnectivityManager
  private lateinit var network: Network
  private lateinit var networkCapabilities: NetworkCapabilities

  @Before
  fun setUp() {
    connectivityManager = mock(ConnectivityManager::class.java)
    network = mock(Network::class.java)
    networkCapabilities = mock(NetworkCapabilities::class.java)

    `when`(connectivityManager.activeNetwork).thenReturn(network)

    // Set the mocked ConnectivityManager for testing
    NetworkUtils.setConnectivityManagerForTesting(connectivityManager)
  }

  @Test
  fun testIsNetworkAvailable_withValidNetwork() {
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        .thenReturn(true)

    assertTrue(NetworkUtils.isNetworkAvailable())
  }

  @Test
  fun testIsNetworkAvailable_withNoNetwork() {
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)

    assertFalse(NetworkUtils.isNetworkAvailable())
  }
}
