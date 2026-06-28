package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeBackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_back)

        // Sembunyikan Action Bar
        supportActionBar?.hide()

        // Inisialisasi tombol Continue
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        // Aksi saat tombol Continue diklik -> Pindah ke Dashboard
        btnContinue.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup halaman ini agar tidak bisa di-back
        }
    }
}