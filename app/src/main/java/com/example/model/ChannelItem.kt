package com.example.model

enum class StreamStatus {
    UNKNOWN,
    CHECKING,
    ONLINE,
    OFFLINE
}

data class ChannelItem(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val group: String = "Umum",
    val userAgent: String = "",
    val httpReferrer: String = "",
    val licenseKey: String = "",
    val licenseType: String = "",
    val status: StreamStatus = StreamStatus.UNKNOWN,
    val latencyMs: Long = 0L,
    val isSelected: Boolean = true
) {
    fun toM3uEntry(): String {
        val sb = StringBuilder()
        val logoAttr = if (logoUrl.isNotBlank()) " tvg-logo=\"$logoUrl\"" else ""
        val groupAttr = if (group.isNotBlank()) " group-title=\"$group\"" else ""
        val nameAttr = " tvg-name=\"$name\""
        
        sb.append("#EXTINF:-1 tvg-id=\"\"$nameAttr$logoAttr$groupAttr,$name\n")
        
        if (userAgent.isNotBlank()) {
            sb.append("#EXTVLCOPT:http-user-agent=$userAgent\n")
        }
        if (httpReferrer.isNotBlank()) {
            sb.append("#EXTVLCOPT:http-referrer=$httpReferrer\n")
        }
        if (licenseKey.isNotBlank()) {
            sb.append("#KODIPROP:inputstream.adaptive.license_key=$licenseKey\n")
        }
        if (licenseType.isNotBlank()) {
            sb.append("#KODIPROP:inputstream.adaptive.license_type=$licenseType\n")
        }
        
        sb.append(streamUrl)
        return sb.toString()
    }
}
