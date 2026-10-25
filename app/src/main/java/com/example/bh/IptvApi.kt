package com.example.bh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface IptvApi {
    @GET("channels.json")
    suspend fun getChannels(): List<ChannelDto>

    @GET("streams.json")
    suspend fun getStreams(): List<StreamDto>
}

object IptvRepository {
    private val api: IptvApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://iptv-org.github.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IptvApi::class.java)
    }

    private var cache: List<Channel>? = null

    suspend fun loadChannels(forceRefresh: Boolean = false): List<Channel> {
        cache?.let { if (!forceRefresh) return it }

        return withContext(Dispatchers.IO) {
            val channelsDeferred = async { api.getChannels() }
            val streamsDeferred = async { api.getStreams() }

            val channels = channelsDeferred.await()
            val streams = streamsDeferred.await().groupBy { it.channelId }

            val result = channels.mapNotNull { c ->
                if (c.isNsfw) return@mapNotNull null
                val stream = streams[c.id]?.firstOrNull() ?: return@mapNotNull null
                Channel(
                    id = c.id,
                    name = c.name,
                    logo = c.logo,
                    country = c.country,
                    category = c.categories?.firstOrNull(),
                    url = stream.url
                )
            }.sortedBy { it.name.lowercase() }

            cache = result
            result
        }
    }
}
