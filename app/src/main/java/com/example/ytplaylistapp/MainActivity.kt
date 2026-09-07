package com.example.ytplaylistapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etOAuthToken = findViewById<EditText>(R.id.etOAuthToken)
        val btnLoadMyPlaylists = findViewById<Button>(R.id.btnLoadMyPlaylists)
        val rvPlaylists = findViewById<RecyclerView>(R.id.rvPlaylists)

        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val savedToken = sharedPrefs.getString("OAUTH_TOKEN", "")
        if (!savedToken.isNullOrEmpty()) {
            etOAuthToken.setText(savedToken)
        }

        rvPlaylists.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(YouTubeApi::class.java)

        btnLoadMyPlaylists.setOnClickListener {
            val token = etOAuthToken.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(this, "Please enter your OAuth Access Token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPrefs.edit().putString("OAUTH_TOKEN", token).apply()
            Toast.makeText(this, "Fetching your account playlists...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val authHeader = "Bearer $token"
                    val response = api.getMyPlaylists(authHeader = authHeader)
                    val items = response.items ?: emptyList()
                    
                    withContext(Dispatchers.Main) {
                        if (items.isEmpty()) {
                            Toast.makeText(this@MainActivity, "No playlists found or token expired.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Loaded ${items.size} playlists from your account!", Toast.LENGTH_SHORT).show()
                        }
                        
                        rvPlaylists.adapter = PlaylistAdapter(items) { playlistId ->
                            openYouTubeMusic(playlistId)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Auth Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun openYouTubeMusic(playlistId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/playlist?list=$playlistId"))
        intent.setPackage("com.google.android.apps.youtube.music")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback if YouTube Music app is not installed
            intent.setPackage(null)
            startActivity(intent)
        }
    }
}
