package com.amurcanov.tgwsproxy

import android.content.Context
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

data class HttpProxyEntry(
    val name: String = "",
    val host: String = "",
    val port: Int = 3128,
    val username: String = "",
    val password: String = "",
    val enabled: Boolean = false,
)

object RuntimeConfig {
    private const val FILE_NAME = "proxy_config.json"

    fun parseProxyList(json: String): List<HttpProxyEntry> {
        val arr = try {
            JSONArray(json.ifBlank { "[]" })
        } catch (_: Exception) {
            JSONArray()
        }
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(
                    HttpProxyEntry(
                        name = obj.optString("name"),
                        host = obj.optString("host"),
                        port = obj.optInt("port", 3128),
                        username = obj.optString("username"),
                        password = obj.optString("password"),
                        enabled = obj.optBoolean("enabled", false),
                    )
                )
            }
        }
    }

    fun encodeProxyList(proxies: List<HttpProxyEntry>): String {
        val arr = JSONArray()
        proxies.forEach { proxy ->
            arr.put(
                JSONObject()
                    .put("name", proxy.name)
                    .put("host", proxy.host)
                    .put("port", proxy.port)
                    .put("username", proxy.username)
                    .put("password", proxy.password)
                    .put("enabled", proxy.enabled)
            )
        }
        return arr.toString()
    }

    fun ensureConfigFile(context: Context, settingsStore: SettingsStore): File {
        val configFile = File(context.filesDir, FILE_NAME)
        val runtimeJson = runBlocking {
            settingsStore.migrateLegacyDefaults()
            val proxies = parseProxyList(settingsStore.httpProxyListJson.first())
            val hasEnabledProxy = proxies.any { it.enabled && it.host.isNotBlank() && it.port > 0 }

            JSONObject()
                .put("transport_mode", if (hasEnabledProxy) "http_proxy_first" else "default")
                .put("http_proxies", JSONArray(encodeProxyList(proxies)))
                .toString(2)
        }
        configFile.writeText(runtimeJson)
        return configFile
    }
}
