package com.example.model

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class ExtractionLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

data class DiscoveredEndpoint(
    val name: String,
    val url: String,
    val type: String, // e.g. "XOR Encrypted M3U", "Xtream API", "Embedded M3U", "Direct Stream"
    val isReachable: Boolean = false,
    val details: String = ""
)

data class ApkMetadata(
    val fileName: String = "",
    val fileSizeFormatted: String = "",
    val dexFilesCount: Int = 0,
    val totalStringsScanned: Int = 0,
    val packageName: String = "",
    val appTitle: String = ""
)

data class ExtractionResult(
    val metadata: ApkMetadata = ApkMetadata(),
    val channels: List<ChannelItem> = emptyList(),
    val endpoints: List<DiscoveredEndpoint> = emptyList(),
    val encryptionKeys: List<String> = emptyList(),
    val rawM3u: String = "",
    val totalChannelsFound: Int = 0,
    val completedAt: Long = System.currentTimeMillis()
)
