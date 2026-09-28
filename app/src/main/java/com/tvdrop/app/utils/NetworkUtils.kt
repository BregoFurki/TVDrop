package com.tvdrop.app.utils

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {

    /**
     * Finds the local IPv4 address of the active Wi-Fi or Ethernet connection on the TV.
     * Prioritizes wlan0 (Wi-Fi) and eth0 (Ethernet).
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            var fallbackIp: String? = null

            for (networkInterface in interfaces) {
                if (!networkInterface.isUp || networkInterface.isLoopback) continue

                val name = networkInterface.name.lowercase()
                val addresses = Collections.list(networkInterface.inetAddresses)

                for (inetAddress in addresses) {
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        val hostAddress = inetAddress.hostAddress ?: continue

                        // Prefer Wi-Fi or Ethernet
                        if (name.startsWith("wlan") || name.startsWith("eth")) {
                            return hostAddress
                        }
                        if (fallbackIp == null) {
                            fallbackIp = hostAddress
                        }
                    }
                }
            }
            return fallbackIp
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
