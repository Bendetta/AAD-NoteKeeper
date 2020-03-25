package com.benliset.notekeeper

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass

class DataManagerTest {

    companion object {
        lateinit var dataManager: DataManager

        @BeforeClass
        @JvmStatic
        fun classSetUp() {
            dataManager = DataManager.instance
        }
    }

    @Before
    fun setUp() {
        dataManager.notes.clear()
        dataManager.initializeExampleNotes()
    }

    @Test
    fun createNewNote() {
        val course = dataManager.getCourse("android_async")
        val noteTitle = "Test note title"
        val noteText = "this is the body text of my test note"

        val noteIndex = dataManager.createNewNote()
        val newNote = dataManager.notes[noteIndex]
        newNote.course = course
        newNote.title = noteTitle
        newNote.text = noteText

        val compareNote = dataManager.notes[noteIndex]
        assertEquals(course, compareNote.course)
        assertEquals(noteTitle, compareNote.title)
        assertEquals(noteText, compareNote.text)
    }

    @Test
    fun findSimilarNotes() {
        val course = dataManager.getCourse("android_async")
        val noteTitle = "Test note title"
        val noteText1 = "this is the body text of my test note"
        val noteText2 = "this is the body text of my second test note"

        val noteIndex1 = dataManager.createNewNote()
        val newNote1 = dataManager.notes[noteIndex1]
        newNote1.course = course
        newNote1.title = noteTitle
        newNote1.text = noteText1

        val noteIndex2 = dataManager.createNewNote()
        val newNote2 = dataManager.notes[noteIndex2]
        newNote2.course = course
        newNote2.title = noteTitle
        newNote2.text = noteText2

        val foundIndex1 = dataManager.findNote(newNote1)
        assertEquals(noteIndex1, foundIndex1)

        val foundIndex2 = dataManager.findNote(newNote2)
        assertEquals(noteIndex2, foundIndex2)
    }

    @Test
    fun createNewNoteOneStepCreation() {
        val course = dataManager.getCourse("android_async")
        val noteTitle = "Test note title"
        val noteText = "this is the body text of my test note"

        val noteIndex = dataManager.createNewNote(course, noteTitle, noteText)

        val compareNote = dataManager.notes[noteIndex]
        assertEquals(course, compareNote.course)
        assertEquals(noteTitle, compareNote.title)
        assertEquals(noteText, compareNote.text)
    }
}