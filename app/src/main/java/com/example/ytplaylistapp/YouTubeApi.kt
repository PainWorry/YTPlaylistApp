package com.example.ytplaylistapp

import retrofit2.http.GET
import retrofit2.http.Query

data class YouTubeResponse(val items: List<VideoItem>?)
data class VideoItem(val id: PlaylistId?, val snippet: Snippet?) {
    val actualPlaylistId: String?
        get() = id?.playlistId
}
data class PlaylistId(val playlistId: String?)
data class Snippet(val title: String?, val description: String?, val channelTitle: String?)

interface YouTubeApi {
    @GET("search")
    suspend fun searchPlaylists(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "playlist",
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 20
    ): YouTubeResponse

    @GET("playlists")
    suspend fun getChannelPlaylists(
        @Query("part") part: String = "snippet",
        @Query("channelId") channelId: String,
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 20
    ): YouTubeResponse
}
