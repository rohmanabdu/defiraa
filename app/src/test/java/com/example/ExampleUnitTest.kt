package com.example

import com.example.model.ChannelItem
import com.example.model.StreamStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testChannelItemToM3uEntry() {
        val channel = ChannelItem(
            id = "1",
            name = "RCTI HD",
            streamUrl = "https://example.com/rcti.m3u8",
            logoUrl = "https://example.com/rcti.png",
            group = "Nasional",
            userAgent = "Mozilla/5.0",
            status = StreamStatus.ONLINE
        )

        val entry = channel.toM3uEntry()
        assertTrue(entry.contains("#EXTINF:-1"))
        assertTrue(entry.contains("tvg-name=\"RCTI HD\""))
        assertTrue(entry.contains("tvg-logo=\"https://example.com/rcti.png\""))
        assertTrue(entry.contains("group-title=\"Nasional\""))
        assertTrue(entry.contains("#EXTVLCOPT:http-user-agent=Mozilla/5.0"))
        assertTrue(entry.contains("https://example.com/rcti.m3u8"))
    }

    @Test
    fun testDeleteSelectedChannelsLogic() {
        val channels = listOf(
            ChannelItem(id = "1", name = "Channel 1", streamUrl = "https://1.com", isSelected = true),
            ChannelItem(id = "2", name = "Channel 2", streamUrl = "https://2.com", isSelected = false),
            ChannelItem(id = "3", name = "Channel 3", streamUrl = "https://3.com", isSelected = true)
        )

        val remaining = channels.filterNot { it.isSelected }
        assertEquals(1, remaining.size)
        assertEquals("Channel 2", remaining[0].name)
    }
}
