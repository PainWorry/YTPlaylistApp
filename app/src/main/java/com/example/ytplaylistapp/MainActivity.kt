package com.example.ytplaylistapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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

    private var allPlaylists: List<VideoItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etOAuthToken = findViewById<EditText>(R.id.etOAuthToken)
        val btnLoadMyPlaylists = findViewById<Button>(R.id.btnLoadMyPlaylists)
        val etFilter = findViewById<EditText>(R.id.etFilter)
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
                    allPlaylists = response.items ?: emptyList()
                    
                    withContext(Dispatchers.Main) {
                        if (allPlaylists.isEmpty()) {
                            Toast.makeText(this@MainActivity, "No playlists found in your account.", Toast.LENGTH_LONG).show()
                            etFilter.visibility = View.GONE
                        } else {
                            Toast.makeText(this@MainActivity, "Loaded ${allPlaylists.size} playlists!", Toast.LENGTH_SHORT).show()
                            etFilter.visibility = View.VISIBLE
                        }
                        
                        updateList(allPlaylists, rvPlaylists)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                val filtered = if (query.isEmpty()) {
                    allPlaylists
                } else {
                    allPlaylists.filter { item: VideoItem ->
                        val titleMatch = item.snippet?.title?.lowercase()?.contains(query) == true
                        val descMatch = item.snippet?.description?.lowercase()?.contains(query) == true
                        titleMatch || descMatch
                    }
                }
                updateList(filtered, rvPlaylists)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateList(items: List<VideoItem>, rv: RecyclerView) {
        rv.adapter = PlaylistAdapter(items) { playlistId ->
            openYouTubeMusic(playlistId)
        }
    }

    private fun openYouTubeMusic(playlistId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/playlist?list=$playlistId"))
        intent.setPackage("com.google.android.apps.youtube.music")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            intent.setPackage(null)
            startActivity(intent)
        }
    }
}
