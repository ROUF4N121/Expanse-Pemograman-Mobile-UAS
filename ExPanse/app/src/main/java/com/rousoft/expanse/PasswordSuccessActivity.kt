package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PasswordSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_success)
        supportActionBar?.hide()

        val btnBack = findViewById<Button>(R.id.btnBackToLoginSuccess)

        btnBack.setOnClickListener {
            // Kembali ke Login dan hapus semua activity sebelumnya
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}