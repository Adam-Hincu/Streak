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
        // Counter views
        val counterValue: TextView? = view.findViewById(R.id.counter_value)
        val counterControls: View? = view.findViewById(R.id.counter_controls)
        val buttonMinusOne: MaterialButton? = view.findViewById(R.id.button_minus_one)
        val buttonPlusOne: MaterialButton? = view.findViewById(R.id.button_plus_one)
        val buttonResetCounter: MaterialButton? = view.findViewById(R.id.button_reset_counter)
        val buttonDeleteCounter: MaterialButton? = view.findViewById(R.id.button_delete_counter)
        val buttonCustomAdd: MaterialButton? = view.findViewById(R.id.button_custom_add)
        val buttonCustomRetract: MaterialButton? = view.findViewById(R.id.button_custom_retract)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.streak_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val streak = streaks[position]
        holder.activityTitle.text = streak.title
        updateStatusIndicator(holder, streak)

        if (streak.type == "counter") {
            // Hide timer UI
            holder.daysCount.visibility = View.GONE
            holder.hoursCount.visibility = View.GONE
            holder.minutesCount.visibility = View.GONE
            holder.minutesLabel.visibility = View.GONE
            holder.secondsCount.visibility = View.GONE
            holder.secondsLabel.visibility = View.GONE
            holder.pauseButton.visibility = View.GONE
            holder.resetButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
            // Show counter UI
            holder.counterValue?.visibility = View.VISIBLE
            holder.counterControls?.visibility = View.VISIBLE
            holder.counterValue?.text = streak.counterValue.toString()
            // Button actions
            holder.buttonPlusOne?.setOnClickListener {
                streak.incrementCounter(1)
                notifyItemChanged(position)
            }
            holder.buttonMinusOne?.setOnClickListener {
                streak.decrementCounter(1)
                notifyItemChanged(position)
            }
            holder.buttonResetCounter?.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle(holder.itemView.context.getString(R.string.reset_title))
                    .setMessage(holder.itemView.context.getString(R.string.reset_message, streak.title))
                    .setPositiveButton(holder.itemView.context.getString(R.string.reset)) { _, _ ->
                        streak.resetCounter()
                        notifyItemChanged(position)
                    }
                    .setNegativeButton(holder.itemView.context.getString(R.string.cancel), null)
                    .show()
            }
            holder.buttonDeleteCounter?.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle(holder.itemView.context.getString(R.string.delete_title))
                    .setMessage(holder.itemView.context.getString(R.string.delete_message, streak.title))
                    .setPositiveButton(holder.itemView.context.getString(R.string.delete)) { _, _ ->
                        val pos = holder.adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            streaks.removeAt(pos)
                            notifyItemRemoved(pos)
                        }
                    }
                    .setNegativeButton(holder.itemView.context.getString(R.string.cancel), null)
                    .show()
            }
            holder.buttonCustomAdd?.setOnClickListener {
                showCustomAmountDialog(holder, streak, position, isAdd = true)
            }
            holder.buttonCustomRetract?.setOnClickListener {
                showCustomAmountDialog(holder, streak, position, isAdd = false)
            }
        } else {
            // Show timer UI
            val time = streak.getTimeParts()
            holder.daysCount.text = time.days.toString()
            holder.hoursCount.text = time.hours.toString()
            holder.minutesCount.text = time.minutes.toString()
            holder.secondsCount.text = time.seconds.toString()
            val showMin = showMinutes()
            val showSec = showSeconds()
            holder.minutesCount.visibility = if (showMin) View.VISIBLE else View.GONE
            holder.minutesLabel.visibility = if (showMin) View.VISIBLE else View.GONE
            holder.secondsCount.visibility = if (showSec) View.VISIBLE else View.GONE
            holder.secondsLabel.visibility = if (showSec) View.VISIBLE else View.GONE
            holder.pauseButton.visibility = View.VISIBLE
            holder.resetButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
            // Hide counter UI
            holder.counterValue?.visibility = View.GONE
            holder.counterControls?.visibility = View.GONE
            updatePauseButtonIcon(holder, streak)
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
            holder.deleteButton.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle(holder.itemView.context.getString(R.string.delete_title))
                    .setMessage(holder.itemView.context.getString(R.string.delete_message, streak.title))
                    .setPositiveButton(holder.itemView.context.getString(R.string.delete)) { _, _ ->
                        val pos = holder.adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            streaks.removeAt(pos)
                            notifyItemRemoved(pos)
                        }
                    }
                    .setNegativeButton(holder.itemView.context.getString(R.string.cancel), null)
                    .show()
            }
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

    private fun showCustomAmountDialog(holder: ViewHolder, streak: StreakItem, position: Int, isAdd: Boolean) {
        val context = holder.itemView.context
        val input = android.widget.EditText(context)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
        val title = if (isAdd) context.getString(R.string.custom_add_title) else context.getString(R.string.custom_retract_title)
        val positive = if (isAdd) context.getString(R.string.add) else context.getString(R.string.retract)
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positive) { _, _ ->
                val value = input.text.toString().toIntOrNull() ?: 0
                if (isAdd) {
                    streak.incrementCounter(value)
                } else {
                    streak.decrementCounter(value)
                }
                notifyItemChanged(position)
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .show()
    }

    override fun getItemCount() = streaks.size
} 