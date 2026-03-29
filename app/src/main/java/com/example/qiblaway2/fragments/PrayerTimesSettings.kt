package com.example.qiblaway2.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.qiblaway2.R
import com.example.qiblaway2.adapter.MethodItem
import com.example.qiblaway2.adapter.RadioListAdapter
import com.example.qiblaway2.databinding.PrayertimessettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PrayerTimesSettings: BottomSheetDialogFragment() {
    private lateinit var binding: PrayertimessettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PrayertimessettingsBinding.inflate(inflater, container, false)

//        binding.btnClose.setOnClickListener {
//            dismiss() // Close the Bottom Sheet
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences setup
        val sharedPrefs = requireContext().getSharedPreferences("prayer_settings", Context.MODE_PRIVATE)
        val selectedMethodId = sharedPrefs.getInt("selected_method_id", 2)
        val selectedMadhhab = sharedPrefs.getInt("selected_madhhab", 1) // 0 = شافعي, 1 = حنفي

        // Prayer Calculation Methods List
        val methodList = listOf(
            MethodItem(0, getString(R.string.method_name_0), getString(R.string.method_desc_0)),
            MethodItem(1, getString(R.string.method_name_1), getString(R.string.method_desc_1)),
            MethodItem(2, getString(R.string.method_name_2), getString(R.string.method_desc_2)),
            MethodItem(3, getString(R.string.method_name_3), getString(R.string.method_desc_3)),
            MethodItem(4, getString(R.string.method_name_4), getString(R.string.method_desc_4)),
            MethodItem(5, getString(R.string.method_name_5), getString(R.string.method_desc_5)),
            MethodItem(7, getString(R.string.method_name_7), getString(R.string.method_desc_7)),
            MethodItem(8, getString(R.string.method_name_8), getString(R.string.method_desc_8)),
            MethodItem(9, getString(R.string.method_name_9), getString(R.string.method_desc_9)),
            MethodItem(10, getString(R.string.method_name_10), getString(R.string.method_desc_10)),
            MethodItem(11, getString(R.string.method_name_11), getString(R.string.method_desc_11)),
            MethodItem(12, getString(R.string.method_name_12), getString(R.string.method_desc_12)),
            MethodItem(13, getString(R.string.method_name_13), getString(R.string.method_desc_13)),
            MethodItem(14, getString(R.string.method_name_14), getString(R.string.method_desc_14))
        )

        // Find position of the saved method id
        val defaultPosition = methodList.indexOfFirst { it.id == selectedMethodId }

        // RecyclerView setup
        binding.rvMethod.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = RadioListAdapter(methodList, selectedMethodId) { selectedMethod ->
            sharedPrefs.edit().putInt("selected_method_id", selectedMethod.id).apply()
            Toast.makeText(requireContext(), "Selected ID: ${selectedMethod.id}", Toast.LENGTH_SHORT).show()
            // أرسل إشعار التغيير
            parentFragmentManager.setFragmentResult("prayer_settings_changed", Bundle())
        }
        binding.rvMethod.adapter = adapter
        binding.rvMethod.scrollToPosition(defaultPosition)

        // RadioGroup (Madhhab) setup
        when (selectedMadhhab) {
            1 -> {binding.radioButtonHanafi.isChecked = true
            }
            0 -> {binding.radioButtonStandard.isChecked = true
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val selectedRadio = view.findViewById<RadioButton>(checkedId)
                val madhhabValue = selectedRadio.tag.toString().toInt()
                sharedPrefs.edit().putInt("selected_madhhab", madhhabValue).apply()
                Toast.makeText(requireContext(), "المذهب: $madhhabValue", Toast.LENGTH_SHORT).show()
                // أرسل إشعار التغيير
                parentFragmentManager.setFragmentResult("prayer_settings_changed", Bundle())
            }
        }
    }



}