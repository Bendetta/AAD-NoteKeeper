package com.benliset.notekeeper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var noteRecyclerAdapter: NoteRecyclerAdapter
    private lateinit var courseRecyclerAdapter: CourseRecyclerAdapter
    private lateinit var recyclerNotes: RecyclerView
    private lateinit var notesLayoutManager: LinearLayoutManager
    private lateinit var coursesLayoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            startActivity(Intent(this, NoteActivity::class.java))
        }
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        initializeDisplayContent()
    }

    override fun onResume() {
        super.onResume()
        noteRecyclerAdapter.notifyDataSetChanged()
    }

    private fun initializeDisplayContent() {
        recyclerNotes = findViewById<RecyclerView>(R.id.list_items)
        notesLayoutManager = LinearLayoutManager(this)
        coursesLayoutManager = GridLayoutManager(this, resources.getInteger(R.integer.course_grid_span))
        val notes = DataManager.instance.notes
        noteRecyclerAdapter = NoteRecyclerAdapter(this, notes)

        val courses = DataManager.instance.courses
        courseRecyclerAdapter = CourseRecyclerAdapter(this, courses)

        displayNotes()
    }

    private fun displayNotes() {
        recyclerNotes.layoutManager = notesLayoutManager
        recyclerNotes.adapter = noteRecyclerAdapter

        selectNavigationMenuItem(R.id.nav_notes)
    }

    private fun selectNavigationMenuItem(id: Int) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        menu.findItem(id).isChecked = true
    }

    private fun displayCourses() {
        recyclerNotes.layoutManager = coursesLayoutManager
        recyclerNotes.adapter = courseRecyclerAdapter

        selectNavigationMenuItem(R.id.nav_courses)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_notes -> {
                displayNotes()
            }
            R.id.nav_courses -> {
                displayCourses()
            }
            R.id.nav_share -> {
                handleSelection(R.string.nav_share_message)
            }
            R.id.nav_send -> {
                handleSelection(R.string.nav_send_message)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleSelection(message_id: Int) {
        val view = findViewById<View>(R.id.list_items)
        Snackbar.make(view, message_id, Snackbar.LENGTH_LONG).show()
    }
}
