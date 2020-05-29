package com.benliset.notekeeper

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.loader.content.CursorLoader
import com.benliset.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.benliset.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.benliset.notekeeper.NoteKeeperProviderContract.Notes

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        val LOADER_NOTES = 0
    }

    private lateinit var noteRecyclerAdapter: NoteRecyclerAdapter
    private lateinit var courseRecyclerAdapter: CourseRecyclerAdapter
    private lateinit var recyclerNotes: RecyclerView
    private lateinit var notesLayoutManager: LinearLayoutManager
    private lateinit var coursesLayoutManager: GridLayoutManager
    private lateinit var dbOpenHelper: NoteKeeperOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        enableStrictMode()

        dbOpenHelper = NoteKeeperOpenHelper(this)
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

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
            StrictMode.setThreadPolicy(policy)
        }
    }

    override fun onDestroy() {
        dbOpenHelper.close()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        supportLoaderManager.restartLoader(LOADER_NOTES, null, this)
        updateNavHeader()

        openDrawer()
    }

    private fun openDrawer() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.openDrawer(GravityCompat.START)
        }, 1000)
    }

    private fun loadNotes() {
        val db = dbOpenHelper.readableDatabase
        val noteColumns = arrayOf(
            NoteInfoEntry.COLUMN_NOTE_TITLE,
            NoteInfoEntry.COLUMN_COURSE_ID,
            NoteInfoEntry._ID
        )
        val noteOrderBy = "${NoteInfoEntry.COLUMN_COURSE_ID}, ${NoteInfoEntry.COLUMN_NOTE_TITLE}"
        val noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, noteOrderBy)
        noteRecyclerAdapter.changeCursor(noteCursor)
    }

    private fun updateNavHeader() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val textUserName = headerView.findViewById<TextView>(R.id.text_user_name)
        val textEmailAddress = headerView.findViewById<TextView>(R.id.text_email_address)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val userName = pref.getString("user_display_name", getString(R.string.pref_default_display_name))
        val emailAddress = pref.getString("user_email_address", getString(R.string.pref_default_email_address))

        textUserName.text = userName
        textEmailAddress.text = emailAddress
    }

    private fun initializeDisplayContent() {
        DataManager.loadFromDatabase(dbOpenHelper)
        recyclerNotes = findViewById<RecyclerView>(R.id.list_items)
        notesLayoutManager = LinearLayoutManager(this)
        coursesLayoutManager = GridLayoutManager(this, resources.getInteger(R.integer.course_grid_span))

        noteRecyclerAdapter = NoteRecyclerAdapter(this, null)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
                handleShare()
            }
            R.id.nav_send -> {
                handleSelection(R.string.nav_send_message)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleShare() {
        val view = findViewById<View>(R.id.list_items)
        Snackbar.make(view,
            "Share to - ${PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social", getString(R.string.pref_default_favorite_social))}",
            Snackbar.LENGTH_LONG).show()
    }

    private fun handleSelection(message_id: Int) {
        val view = findViewById<View>(R.id.list_items)
        Snackbar.make(view, message_id, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // if (id == LOADER_NOTES)
        val noteColumns = arrayOf(
            Notes._ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_COURSE_TITLE
        )

        val noteOrderBy = "${Notes.COLUMN_COURSE_TITLE}, ${Notes.COLUMN_NOTE_TITLE}"
        return CursorLoader(this, Notes.CONTENT_EXPANDED_URI, noteColumns, null, null, noteOrderBy)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (loader.id == LOADER_NOTES) {
            noteRecyclerAdapter.changeCursor(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTES) {
            noteRecyclerAdapter.changeCursor(null)
        }
    }
}
