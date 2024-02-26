package com.example.step.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.step.HeaderActivity
import com.example.step.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var editTextDream: EditText
    private lateinit var editTextWeight: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        editTextDream = view.findViewById(R.id.editTextDream)
        editTextWeight = view.findViewById(R.id.editTextWeight)

        val sharedPreferences = activity?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val dreamValue = sharedPreferences?.getString("dream", "")
        val weightValue = sharedPreferences?.getString("weight", "")

        editTextDream.setText(dreamValue)
        editTextWeight.setText(weightValue)

        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val editor = sharedPreferences?.edit()
            editor?.putString("dream", editTextDream.text.toString())
            editor?.putString("weight", editTextWeight.text.toString())
            editor?.apply()

            Toast.makeText(activity, "Сохранение успешно", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), HeaderActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

            activity?.finish()

        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
