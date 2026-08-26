package com.example.extractor

import com.example.model.ChannelItem
import com.example.model.StreamStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class StreamTester {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun testStream(channel: ChannelItem): ChannelItem = withContext(Dispatchers.IO) {
        if (channel.streamUrl.isBlank()) {
            return@withContext channel.copy(status = StreamStatus.OFFLINE)
        }

        val startTime = System.currentTimeMillis()
        try {
            val reqBuilder = Request.Builder()
                .url(channel.streamUrl)
                .head() // HEAD request is fast and lightweight

            if (channel.userAgent.isNotBlank()) {
                reqBuilder.header("User-Agent", channel.userAgent)
            }
            if (channel.httpReferrer.isNotBlank()) {
                reqBuilder.header("Referer", channel.httpReferrer)
            }

            val response = client.newCall(reqBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime

            val isOnline = response.isSuccessful || response.code in 200..399
            channel.copy(
                status = if (isOnline) StreamStatus.ONLINE else StreamStatus.OFFLINE,
                latencyMs = latency
            )
        } catch (e: Exception) {
            // Try GET with range 0-100 bytes in case HEAD is not allowed
            try {
                val reqBuilder = Request.Builder()
                    .url(channel.streamUrl)
                    .header("Range", "bytes=0-100")
                if (channel.userAgent.isNotBlank()) {
                    reqBuilder.header("User-Agent", channel.userAgent)
                }

                val response = client.newCall(reqBuilder.build()).execute()
                val latency = System.currentTimeMillis() - startTime
                val isOnline = response.isSuccessful || response.code in 200..399
                channel.copy(
                    status = if (isOnline) StreamStatus.ONLINE else StreamStatus.OFFLINE,
                    latencyMs = latency
                )
            } catch (e2: Exception) {
                channel.copy(
                    status = StreamStatus.OFFLINE,
                    latencyMs = 0L
                )
            }
        }
    }
}
