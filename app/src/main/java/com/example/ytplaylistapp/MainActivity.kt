package com.example.ytplaylistapp

import android.content.Context
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
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
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val rvPlaylists = findViewById<RecyclerView>(R.id.rvPlaylists)
        val webView = findViewById<WebView>(R.id.webViewPlayer)

        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val savedKey = sharedPrefs.getString("API_KEY", "")
        if (!savedKey.isNullOrEmpty()) {
            etApiKey.setText(savedKey)
        }

        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        rvPlaylists.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(YouTubeApi::class.java)

        btnSearch.setOnClickListener {
            val apiKey = etApiKey.text.toString().trim()
            val query = etSearch.text.toString().trim()

            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter your YouTube API Key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPrefs.edit().putString("API_KEY", apiKey).apply()

            if (query.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = api.searchPlaylists(query = query, apiKey = apiKey)
                        val items = response.items ?: emptyList()
                        withContext(Dispatchers.Main) {
                            rvPlaylists.adapter = PlaylistAdapter(items) { playlistId ->
                                val html = "<body style=\"margin:0;padding:0;background-color:black;\"><iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed?listType=playlist&list=$playlistId\" frameborder=\"0\" allowfullscreen></iframe></body>"
                                webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
