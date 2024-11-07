package com.github.se.icebreakrr.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.* //

// Tests written with the help of CursorAI, I know the tests may be weird since everything is mocked
// but I don't know how else to test
class NetworkUtilsTest {

  private lateinit var context: Context
  private lateinit var connectivityManager: ConnectivityManager
  private lateinit var network: Network
  private lateinit var networkCapabilities: NetworkCapabilities

  @Before
  fun setUp() {
    context = mock(Context::class.java)
    connectivityManager = mock(ConnectivityManager::class.java)
    network = mock(Network::class.java)
    networkCapabilities = mock(NetworkCapabilities::class.java)

    `when`(context.getSystemService(ConnectivityManager::class.java))
        .thenReturn(connectivityManager)
    `when`(connectivityManager.activeNetwork).thenReturn(network)
  }

  @Test
  fun testIsNetworkAvailable_withValidNetwork() {
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        .thenReturn(true)

    assertTrue(NetworkUtils.isNetworkAvailable(context))
  }

  @Test
  fun testIsNetworkAvailable_withNoNetwork() {
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)

    assertFalse(NetworkUtils.isNetworkAvailable(context))
  }
}
