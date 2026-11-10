package com.example.lab3_storage

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.widget.EditText
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class LabRegressionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private lateinit var activity: Activity
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun launch(type: Class<out Activity>) {
        activity = instrumentation.startActivitySync(Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        main { activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        Thread.sleep(1200)
        instrumentation.waitForIdleSync()
        current()
    }
    private fun enter(id: Int, text: String) = main { activity.findViewById<EditText>(id).setText(text) }
    private fun click(id: Int) {
        Thread.sleep(2400)
        main { activity.findViewById<android.view.View>(id).performClick() }
        instrumentation.waitForIdleSync()
        Thread.sleep(350)
    }
    private fun text(id: Int): String {
        var result = ""
        main { result = activity.findViewById<TextView>(id).text.toString() }
        return result
    }
    private fun current() {
        main { activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).first() }
    }
    private fun rotate() {
        val old = activity
        main { activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        Thread.sleep(1800)
        instrumentation.waitForIdleSync()
        current()
        assertNotSame("Activity must recreate on rotation", old, activity)
    }
    private fun screenshot(name: String) {
        instrumentation.waitForIdleSync()
        Thread.sleep(1000)
        val file = File(context.getExternalFilesDir(null), "audit/$name.png")
        file.parentFile!!.mkdirs()
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    private fun finish() {
        main { activity.finish() }
        instrumentation.waitForIdleSync()
    }
    private fun field(name: String): Any? {
        val field = activity.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(activity)
    }

    @Test fun storageAndRecycler() {
        launch(MainActivity::class.java)
        context.deleteFile("my_note.txt")
        click(R.id.btnClearSP)
        click(R.id.btnLoadSP)
        assertTrue(text(R.id.tvResult).contains("нет"))
        screenshot("01-no-preferences")
        click(R.id.btnLoadFile)
        assertTrue(text(R.id.tvResult).contains("не создан"))
        screenshot("02-no-file")
        enter(R.id.etNote, "   ")
        click(R.id.btnSaveFile)
        assertFalse(File(context.filesDir, "my_note.txt").exists())
        screenshot("03-empty-input")
        enter(R.id.etNote, "Preferences test")
        click(R.id.btnSaveSP)
        finish()
        launch(MainActivity::class.java)
        click(R.id.btnLoadSP)
        assertTrue(text(R.id.tvResult).contains("Preferences test"))
        screenshot("04-preferences-loaded")
        enter(R.id.etNote, "File test")
        click(R.id.btnSaveFile)
        click(R.id.btnLoadFile)
        assertTrue(text(R.id.tvResult).contains("File test"))
        assertEquals("File test", File(context.filesDir, "my_note.txt").readText())
        screenshot("05-file-loaded")
        val noteFile = File(context.filesDir, "my_note.txt")
        noteFile.delete()
        assertTrue(noteFile.mkdir())
        enter(R.id.etNote, "Write error test")
        click(R.id.btnSaveFile)
        screenshot("05-file-write-error")
        click(R.id.btnLoadFile)
        screenshot("05-file-read-error")
        assertTrue(noteFile.isDirectory)
        assertTrue(noteFile.delete())
        click(R.id.btnClearSP)
        screenshot("06-cleared")
        click(R.id.btnOpenRecycler)
        current()
        val list = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvNotes)
        val count = list.adapter!!.itemCount
        enter(R.id.etNewNote, "New list note")
        click(R.id.btnAdd)
        assertEquals(count + 1, list.adapter!!.itemCount)
        screenshot("07-list-added")
        main { list.findViewHolderForAdapterPosition(0)!!.itemView.findViewById<android.view.View>(R.id.btnDelete).performClick() }
        instrumentation.waitForIdleSync()
        assertEquals(count, list.adapter!!.itemCount)
        screenshot("08-list-deleted")
        rotate()
        assertEquals(count, activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvNotes).adapter!!.itemCount)
        finish()
    }
}
