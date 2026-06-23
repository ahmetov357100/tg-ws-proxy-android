package com.amurcanov.tgwsproxy

import android.content.Context
import java.io.File

object RuntimeConfig {
    private const val ASSET_NAME = "proxy_config.json"
    private const val LOCAL_ASSET_NAME = "proxy_config.local.json"
    private const val FILE_NAME = "proxy_config.json"

    fun ensureConfigFile(context: Context): File {
        val configFile = File(context.filesDir, FILE_NAME)
        if (!configFile.exists()) {
            val assetName = try {
                context.assets.open(LOCAL_ASSET_NAME).close()
                LOCAL_ASSET_NAME
            } catch (_: Exception) {
                ASSET_NAME
            }
            context.assets.open(assetName).use { input ->
                configFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return configFile
    }
}
