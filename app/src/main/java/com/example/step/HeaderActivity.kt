package com.example.step

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HeaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header)

        val button = findViewById<Button>(R.id.buttongo)
        val editTextDream = findViewById<EditText>(R.id.dream)
        val editTextWeight = findViewById<EditText>(R.id.weight)

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedDream = sharedPreferences.getString("dream", null)
        val savedWeight = sharedPreferences.getString("weight", null)

        if (!savedDream.isNullOrBlank() && !savedWeight.isNullOrBlank()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        button.setOnClickListener {
            val inputDreamStr = editTextDream.text.toString()
            val inputWeightStr = editTextWeight.text.toString()

            if (inputDreamStr.isEmpty() || inputWeightStr.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val inputDream = inputDreamStr.toDouble()
                    val inputWeight = inputWeightStr.toDouble()

                    val editor = sharedPreferences.edit()
                    editor.putString("dream", inputDreamStr)
                    editor.putString("weight", inputWeightStr)
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Введите корректные числовые значения", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
