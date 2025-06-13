package com.example.streak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class BottomSheetCounterOptions(
    private val streak: StreakItem,
    private val onAddClicked: (Int) -> Unit,
    private val onRetractClicked: (Int) -> Unit,
    private val onResetClicked: () -> Unit,
    private val onDeleteClicked: () -> Unit
) : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_counter_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker).apply {
            minValue = 1
            maxValue = 100
            value = 1
            wrapSelectorWheel = false
            // We'll use XML styling instead of reflection
        }
        
        view.findViewById<MaterialButton>(R.id.button_add_custom).setOnClickListener {
            onAddClicked(numberPicker.value)
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.button_remove_custom).setOnClickListener {
            onRetractClicked(numberPicker.value)
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.button_reset).setOnClickListener {
            onResetClicked()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.button_delete).setOnClickListener {
            onDeleteClicked()
            dismiss()
        }
    }
} 