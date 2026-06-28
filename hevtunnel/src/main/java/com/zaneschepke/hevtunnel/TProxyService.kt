package com.zaneschepke.hevtunnel

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object TProxyService {
    private const val HEV_CONFIG_FILE_NAME: String = "tproxy.conf"
    private const val TASK_STACK_SIZE = 24576

    init {
        System.loadLibrary("hev-socks5-tunnel")
    }

    @JvmStatic external fun TProxyStartService(config_path: String?, fd: Int)

    @JvmStatic external fun TProxyStopService()

    @JvmStatic external fun TProxyGetStats(): LongArray?

    @Throws(IOException::class)
    fun createHevTunnelConfig(config: HevTunnelConfig, cacheDirPath: File): File {
        val tproxyFile = File(cacheDirPath, HEV_CONFIG_FILE_NAME)

        // SNI/TLS routing: only activate when sniHost is set and the SOCKS5 upstream
        // is a real remote server (not the localhost kill-switch bridge). This wires the
        // tunnel's "Domain Masking" field into the HEV SOCKS5 TLS Client Hello so that
        // upstream SOCKS5 servers with SNI-based routing receive the desired server-name.
        val useSniTls = config.sniHost.isNotBlank() && config.address != "127.0.0.1"
        val tlsSection = if (useSniTls) {
            """
          tls:
            allow-insecure: false
            server-name: '${config.sniHost}'"""
        } else ""

        val hevConf =
            """
        misc:
          task-stack-size: $TASK_STACK_SIZE
        tunnel:
          mtu: ${config.mtu}
          ipv4: '${config.ipv4}'
          ipv6: '${config.ipv6}'
        socks5:
          address: '${config.address}'
          port: ${config.port}
          username: '${config.username}'
          password: '${config.password}'
          udp: 'udp'$tlsSection
    """
                .trimIndent()

        FileOutputStream(tproxyFile, false).use { it.write(hevConf.toByteArray()) }
        return tproxyFile
    }
}
