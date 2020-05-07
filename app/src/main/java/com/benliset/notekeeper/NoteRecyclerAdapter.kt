package com.benliset.notekeeper

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benliset.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import kotlin.properties.Delegates

class NoteRecyclerAdapter(private val context: Context, private var cursor: Cursor?)
    : RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>() {

    private var idPos by Delegates.notNull<Int>()
    private var noteTitlePos by Delegates.notNull<Int>()
    private var coursePos by Delegates.notNull<Int>()
    private val layoutInflater = LayoutInflater.from(context)

    init {
        populateColumnPositions()
    }

    private fun populateColumnPositions() {
        // Get column indexes from cursor
        cursor?.let {
            coursePos = it.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
            noteTitlePos = it.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
            idPos = it.getColumnIndex(NoteInfoEntry._ID)
        }
    }

    public fun changeCursor(cursor: Cursor?) {
        this.cursor?.close()
        this.cursor = cursor
        populateColumnPositions()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_note_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor?.let {
            it.moveToPosition(position)
            val course = it.getString(coursePos)
            val noteTitle = it.getString(noteTitlePos)
            val id = it.getInt(idPos)

            holder.textCourse.text = course
            holder.textTitle.text = noteTitle
            holder.id = id
        }
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