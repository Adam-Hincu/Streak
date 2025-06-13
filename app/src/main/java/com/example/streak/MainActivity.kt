package com.example.streak

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.streak.databinding.ActivityMainBinding
import com.example.streak.databinding.DialogAddStreakBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var streakAdapter: StreakAdapter
    private val streakItems = mutableListOf<StreakItem>()
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            streakAdapter.notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    private var showSeconds: Boolean = true
    private var showMinutes: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        binding.streaksRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load streaks from storage
        loadStreaks()
        loadSettings()

        // Initialize adapter
        streakAdapter = StreakAdapter(streakItems, ::showSeconds, ::showMinutes)
        binding.streaksRecyclerView.adapter = streakAdapter

        // Set up FAB to show dialog for creating new streak
        binding.fab.setOnClickListener {
            showAddStreakDialog()
        }

        // Set up three dots button to open settings
        binding.moreButton.setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }

        // Start the timer update
        startTimerUpdates()
    }

    override fun onResume() {
        super.onResume()
        // Reload settings in case they changed in SettingsActivity
        loadSettings()
        streakAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        saveStreaks()
    }

    override fun onStop() {
        super.onStop()
        saveStreaks()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerUpdates()
        saveStreaks()
    }

    private fun startTimerUpdates() {
        handler.post(updateTimerRunnable)
    }

    private fun stopTimerUpdates() {
        handler.removeCallbacks(updateTimerRunnable)
    }

    private fun showAddStreakDialog() {
        val dialogBinding = DialogAddStreakBinding.inflate(LayoutInflater.from(this))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_streak_title))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val title = dialogBinding.editTextTitle.text.toString()
                if (title.isNotEmpty()) {
                    addNewStreak(title)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun addNewStreak(title: String) {
        val newStreak = StreakItem(title = title)
        streakItems.add(newStreak)
        streakAdapter.notifyItemInserted(streakItems.size - 1)
        binding.streaksRecyclerView.smoothScrollToPosition(streakItems.size - 1)
        saveStreaks()
    }

    private fun saveStreaks() {
        val arr = JSONArray()
        streakItems.forEach { arr.put(it.toJson()) }
        getPrefs().edit().putString("streaks", arr.toString()).apply()
    }

    private fun loadStreaks() {
        streakItems.clear()
        val json = getPrefs().getString("streaks", null)
        if (!json.isNullOrEmpty()) {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                streakItems.add(StreakItem.fromJson(obj))
            }
        }
    }

    private fun saveSettings() {
        getPrefs().edit()
            .putBoolean("show_seconds", showSeconds)
            .putBoolean("show_minutes", showMinutes)
            .apply()
    }

    private fun loadSettings() {
        val prefs = getPrefs()
        showSeconds = prefs.getBoolean("show_seconds", true)
        showMinutes = prefs.getBoolean("show_minutes", true)
    }

    private fun getPrefs() = getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
}