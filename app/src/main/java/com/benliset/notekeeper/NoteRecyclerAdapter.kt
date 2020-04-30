package com.benliset.notekeeper

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRecyclerAdapter(private val context: Context, private val notes: List<NoteInfo>)
    : RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_note_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.textCourse.text = note.course?.title
        holder.textTitle.text = note.title
        holder.id = note.id ?: -1
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textCourse = itemView.findViewById<TextView>(R.id.text_course)
        val textTitle = itemView.findViewById<TextView>(R.id.text_title)
        var id = -1

        init {
            itemView.setOnClickListener { view ->
                val intent = Intent(context, NoteActivity::class.java)
                intent.putExtra(NoteActivity.NOTE_ID, id)
                context.startActivity(intent)
            }
        }
    }
}