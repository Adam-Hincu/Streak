package com.example.streak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StreakAdapter(
    private val streaks: MutableList<StreakItem>,
    private val showSeconds: () -> Boolean,
    private val showMinutes: () -> Boolean
) : RecyclerView.Adapter<StreakAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTitle: TextView = view.findViewById(R.id.activity_title)
        val statusIndicator: View = view.findViewById(R.id.status_indicator)
        val daysCount: TextView = view.findViewById(R.id.days_count)
        val hoursCount: TextView = view.findViewById(R.id.hours_count)
        val minutesCount: TextView = view.findViewById(R.id.minutes_count)
        val minutesLabel: TextView = view.findViewById(R.id.minutes_label)
        val secondsCount: TextView = view.findViewById(R.id.seconds_count)
        val secondsLabel: TextView = view.findViewById(R.id.seconds_label)
        val pauseButton: MaterialButton = view.findViewById(R.id.pause_button)
        val resetButton: MaterialButton = view.findViewById(R.id.reset_button)
        val deleteButton: MaterialButton = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.streak_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val streak = streaks[position]
        
        // Set activity title
        holder.activityTitle.text = streak.title
        
        // Set status indicator color
        updateStatusIndicator(holder, streak)
        
        // Set time values
        val time = streak.getTimeParts()
        holder.daysCount.text = time.days.toString()
        holder.hoursCount.text = time.hours.toString()
        holder.minutesCount.text = time.minutes.toString()
        holder.secondsCount.text = time.seconds.toString()
        
        // Show/hide minutes and seconds columns
        val showMin = showMinutes()
        val showSec = showSeconds()
        holder.minutesCount.visibility = if (showMin) View.VISIBLE else View.GONE
        holder.minutesLabel.visibility = if (showMin) View.VISIBLE else View.GONE
        holder.secondsCount.visibility = if (showSec) View.VISIBLE else View.GONE
        holder.secondsLabel.visibility = if (showSec) View.VISIBLE else View.GONE
        
        // Set appropriate icon for pause/play button
        updatePauseButtonIcon(holder, streak)
        
        // Set up click listener for pause button
        holder.pauseButton.setOnClickListener {
            val context = holder.itemView.context
            val isPausing = streak.isActive
            val dialogTitle = if (isPausing) context.getString(R.string.pause_title) else context.getString(R.string.resume_title)
            val dialogMessage = if (isPausing)
                context.getString(R.string.pause_message, streak.title)
            else
                context.getString(R.string.resume_message, streak.title)
            val positiveText = context.getString(R.string.yes)
            val negativeText = context.getString(R.string.no)

            MaterialAlertDialogBuilder(context)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(positiveText) { _, _ ->
                    val now = System.currentTimeMillis()
                    if (isPausing) {
                        streak.pause(now)
                    } else {
                        streak.resume(now)
                    }
                    updateStatusIndicator(holder, streak)
                    updatePauseButtonIcon(holder, streak)
                }
                .setNegativeButton(negativeText, null)
                .show()
        }
        
        // Set up click listener for reset button
        holder.resetButton.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle(holder.itemView.context.getString(R.string.reset_title))
                .setMessage(holder.itemView.context.getString(R.string.reset_message, streak.title))
                .setPositiveButton(holder.itemView.context.getString(R.string.reset)) { _, _ ->
                    streak.reset(System.currentTimeMillis())
                    notifyItemChanged(position)
                }
                .setNegativeButton(holder.itemView.context.getString(R.string.cancel), null)
                .show()
        }
        
        // Set up click listener for delete button
        holder.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle(holder.itemView.context.getString(R.string.delete_title))
                .setMessage(holder.itemView.context.getString(R.string.delete_message, streak.title))
                .setPositiveButton(holder.itemView.context.getString(R.string.delete)) { _, _ ->
                    val position = holder.adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        streaks.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
                .setNegativeButton(holder.itemView.context.getString(R.string.cancel), null)
                .show()
        }
    }
    
    private fun updateStatusIndicator(holder: ViewHolder, streak: StreakItem) {
        val statusColor = if (streak.isActive) {
            R.color.green_active
        } else {
            R.color.grey_inactive
        }
        holder.statusIndicator.backgroundTintList = 
            ContextCompat.getColorStateList(holder.statusIndicator.context, statusColor)
    }
    
    private fun updatePauseButtonIcon(holder: ViewHolder, streak: StreakItem) {
        if (streak.isActive) {
            holder.pauseButton.setIconResource(R.drawable.ic_pause)
        } else {
            holder.pauseButton.setIconResource(R.drawable.ic_play)
        }
    }

    override fun getItemCount() = streaks.size
} 