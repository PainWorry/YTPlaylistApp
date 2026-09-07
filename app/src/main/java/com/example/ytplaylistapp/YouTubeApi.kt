package com.example.ytplaylistapp

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class YouTubeResponse(val items: List<VideoItem>?)
data class VideoItem(val id: String?, val snippet: Snippet?)
data class Snippet(val title: String?, val description: String?)

interface YouTubeApi {
    @GET("playlists?part=snippet")
    suspend fun getMyPlaylists(
        @Header("Authorization") authHeader: String,
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 50
    ): YouTubeResponse
}
