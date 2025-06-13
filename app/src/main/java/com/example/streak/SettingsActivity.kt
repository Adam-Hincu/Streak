package com.example.streak

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.streak.databinding.ActivitySettingsBinding
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // Load and set switch states
        val prefs = getSharedPreferences("streak_prefs", MODE_PRIVATE)
        binding.switchShowSeconds.isChecked = prefs.getBoolean("show_seconds", true)
        binding.switchShowMinutes.isChecked = prefs.getBoolean("show_minutes", true)

        binding.switchShowSeconds.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_seconds", isChecked).apply()
        }
        binding.switchShowMinutes.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_minutes", isChecked).apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
} 