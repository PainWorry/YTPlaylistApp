package com.example.ytplaylistapp

import android.content.Context
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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

        val etApiKey = findViewById<EditText>(R.id.etApiKey)
        val etQuery = findViewById<EditText>(R.id.etQuery)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val btnChannel = findViewById<Button>(R.id.btnChannel)
        val rvPlaylists = findViewById<RecyclerView>(R.id.rvPlaylists)
        val webView = findViewById<WebView>(R.id.webViewPlayer)

        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val savedKey = sharedPrefs.getString("API_KEY", "")
        if (!savedKey.isNullOrEmpty()) {
            etApiKey.setText(savedKey)
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        rvPlaylists.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(YouTubeApi::class.java)

        fun validateAndSaveKey(): String? {
            val key = etApiKey.text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(this, "Please enter your YouTube API Key", Toast.LENGTH_SHORT).show()
                return null
            }
            sharedPrefs.edit().putString("API_KEY", key).apply()
            return key
        }

        btnSearch.setOnClickListener {
            val apiKey = validateAndSaveKey() ?: return@setOnClickListener
            val query = etQuery.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "Enter a search keyword", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Searching public playlists...", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.searchPlaylists(query = query, apiKey = apiKey)
                    val items = response.items ?: emptyList()
                    withContext(Dispatchers.Main) {
                        if (items.isEmpty()) {
                            Toast.makeText(this@MainActivity, "API Key works, but no playlists found.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "API Key is valid! Found ${items.size} playlists.", Toast.LENGTH_SHORT).show()
                        }
                        rvPlaylists.adapter = PlaylistAdapter(items) { playlistId ->
                            loadPlayer(webView, playlistId)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "API Error (Check Key): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnChannel.setOnClickListener {
            val apiKey = validateAndSaveKey() ?: return@setOnClickListener
            val channelId = etQuery.text.toString().trim()
            if (channelId.isEmpty()) {
                Toast.makeText(this, "Enter your YouTube Channel ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Fetching channel playlists...", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.getChannelPlaylists(channelId = channelId, apiKey = apiKey)
                    val items = response.items ?: emptyList()
                    withContext(Dispatchers.Main) {
                        if (items.isEmpty()) {
                            Toast.makeText(this@MainActivity, "No playlists found for this Channel ID.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Loaded ${items.size} channel playlists!", Toast.LENGTH_SHORT).show()
                        }
                        rvPlaylists.adapter = PlaylistAdapter(items) { playlistId ->
                            loadPlayer(webView, playlistId)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error (Check Channel ID/Key): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadPlayer(webView: WebView, playlistId: String) {
        val html = "<body style=\"margin:0;padding:0;background-color:black;\"><iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed?listType=playlist&list=$playlistId\" frameborder=\"0\" allowfullscreen></iframe></body>"
        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
        Toast.makeText(this, "Loading playlist in player...", Toast.LENGTH_SHORT).show()
    }
}
