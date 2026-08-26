package com.example.model

enum class CategorySortMode {
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC,
    CHANNEL_COUNT_DESC,
    CHANNEL_COUNT_ASC,
    CUSTOM
}

data class CategoryInfo(
    val name: String,
    val channelCount: Int,
    val onlineCount: Int = 0
)
