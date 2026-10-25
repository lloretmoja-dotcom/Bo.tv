package com.example.bh

import com.google.gson.annotations.SerializedName

data class Channel(
    val id: String,
    val name: String,
    val logo: String?,
    val country: String?,
    val category: String?,
    val url: String
)

data class ChannelDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("is_nsfw") val isNsfw: Boolean = false
)

data class StreamDto(
    @SerializedName("channel") val channelId: String?,
    @SerializedName("url") val url: String,
    @SerializedName("quality") val quality: String?
)
