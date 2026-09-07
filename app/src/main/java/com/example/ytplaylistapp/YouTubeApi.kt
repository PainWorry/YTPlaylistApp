package com.example.ytplaylistapp

import retrofit2.http.GET
import retrofit2.http.Query

data class YouTubeResponse(val items: List<VideoItem>?)
data class VideoItem(val id: Id?, val snippet: Snippet?)
data class Id(val playlistId: String?)
data class Snippet(val title: String?, val description: String?)

interface YouTubeApi {
    @GET("search")
    suspend fun searchPlaylists(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "playlist",
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("maxResults") maxResults: Int = 15
    ): YouTubeResponse
}
