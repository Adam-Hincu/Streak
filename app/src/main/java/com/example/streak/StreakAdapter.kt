package com.example.streak

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.animation.doOnEnd
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.FragmentActivity
import kotlin.math.abs

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
        val counterContainer: View? = view.findViewById(R.id.counter_container)
        val counterValue: TextView? = view.findViewById(R.id.counter_value)
        val counterActionButton: MaterialButton? = view.findViewById(R.id.counter_action_button)
        val swipeHintLeft: View? = view.findViewById(R.id.swipe_hint_left)
        val swipeHintRight: View? = view.findViewById(R.id.swipe_hint_right)
        
        // For managing gestures
        var gestureDetector: GestureDetectorCompat? = null
        
        // Animation states
        var isAnimating = false
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
            holder.counterContainer?.visibility = View.VISIBLE
            holder.counterActionButton?.visibility = View.VISIBLE
            holder.counterValue?.text = streak.counterValue.toString()
            // Setup gesture detection for swipe left/right to change counter
            setupCounterGestures(holder, streak, position)
            // Setup action button to show bottom sheet
            holder.counterActionButton?.setOnClickListener {
                showCounterOptionsSheet(holder, streak, position)
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
            holder.counterContainer?.visibility = View.GONE
            holder.counterActionButton?.visibility = View.GONE
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
    
    private fun setupCounterGestures(holder: ViewHolder, streak: StreakItem, position: Int) {
        val counterContainer = holder.counterContainer ?: return
        
        // Set up gesture detector
        holder.gestureDetector = GestureDetectorCompat(
            counterContainer.context, 
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (holder.isAnimating) return false
                    
                    // Check horizontal swipe
                    val distanceX = e2.x - (e1?.x ?: 0f)
                    if (abs(distanceX) > 100) { // Minimum swipe distance
                        if (distanceX > 0) {
                            // Swipe right - increment
                            animateCounterChange(holder, streak, position, true)
                        } else {
                            // Swipe left - decrement
                            animateCounterChange(holder, streak, position, false)
                        }
                        return true
                    }
                    return false
                }
            }
        )
        
        // Set touch listener to detect swipes
        counterContainer.setOnTouchListener { _, event ->
            holder.gestureDetector?.onTouchEvent(event) ?: false
        }
    }
    
    private fun animateCounterChange(holder: ViewHolder, streak: StreakItem, position: Int, isIncrement: Boolean) {
        if (holder.isAnimating) return
        
        holder.isAnimating = true
        val counterText = holder.counterValue ?: return
        
        // Animate the counter value out
        val animOut = ObjectAnimator.ofFloat(
            counterText,
            "alpha",
            1f, 0f
        ).apply {
            duration = 150
            doOnEnd {
                // Update the counter value
                if (isIncrement) {
                    streak.incrementCounter(1)
                } else {
                    streak.decrementCounter(1)
                }
                counterText.text = streak.counterValue.toString()
                
                // Animate the new value in
                ObjectAnimator.ofFloat(
                    counterText,
                    "alpha",
                    0f, 1f
                ).apply { 
                    duration = 150
                    doOnEnd {
                        holder.isAnimating = false
                    }
                }.start()
            }
        }
        animOut.start()
    }
    
    private fun showCounterOptionsSheet(holder: ViewHolder, streak: StreakItem, position: Int) {
        val activity = holder.itemView.context as? FragmentActivity ?: return
        
        val bottomSheet = BottomSheetCounterOptions(
            streak = streak,
            onAddClicked = { amount ->
                streak.incrementCounter(amount)
                notifyItemChanged(position)
            },
            onRetractClicked = { amount ->
                streak.decrementCounter(amount)
                notifyItemChanged(position)
            },
            onResetClicked = {
                streak.resetCounter()
                notifyItemChanged(position)
            },
            onDeleteClicked = {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    streaks.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        )
        
        bottomSheet.show(activity.supportFragmentManager, "CounterOptionsBottomSheet")
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