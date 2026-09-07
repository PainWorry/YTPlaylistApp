package com.example.ytplaylistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    private val playlists: List<VideoItem>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = playlists[position]
        holder.tvTitle.text = item.snippet?.title ?: "Untitled Playlist"
        holder.tvDescription.text = item.snippet?.description ?: ""
        holder.itemView.setOnClickListener {
            item.id?.playlistId?.let { playlistId ->
                onClick(playlistId)
            }
        }
    }

    override fun getItemCount() = playlists.size
}
