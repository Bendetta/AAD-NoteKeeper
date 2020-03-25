package com.benliset.notekeeper

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.BeforeClass

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import org.hamcrest.Matchers.*
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.*

@RunWith(AndroidJUnit4::class)
class NoteCreationTest {

    companion object {
        lateinit var dataManager: DataManager

        @BeforeClass
        @JvmStatic
        fun classSetUp() {
            dataManager = DataManager.instance
        }
    }

    @get:Rule
    val noteListActivityRule = ActivityTestRule(NoteListActivity::class.java)

    @Test
    fun createNewNote() {
        val course = dataManager.getCourse("java_lang")!!
        val noteTitle = "Test note title"
        val noteText = "This is the body of our test note"

        onView(withId(R.id.fab)).perform(click())

        onView(withId(R.id.spinner_courses)).perform(click())
        onData(allOf(instanceOf(CourseInfo::class.java), equalTo(course))).perform(click())
        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(containsString(course.title))))

        onView(withId(R.id.text_note_title)).perform(typeText(noteTitle))
            .check(matches(withText(containsString(noteTitle))))
        onView(withId(R.id.text_note_text)).perform(typeText(noteText),
            closeSoftKeyboard())
        onView(withId(R.id.text_note_text)).check(matches(withText(containsString(noteText))))

        pressBack()

        val noteIndex = dataManager.notes.size - 1
        val note = dataManager.notes[noteIndex]
        assertEquals(noteTitle, note.title)
        assertEquals(noteText, note.text)
    }
}