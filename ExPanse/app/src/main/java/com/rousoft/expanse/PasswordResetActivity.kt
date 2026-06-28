package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PasswordResetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)
        supportActionBar?.hide()

        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBackToLoginOtp)

        // Mengambil email dari Intent
        val email = intent.getStringExtra("USER_EMAIL")
        // Saya sedikit menyesuaikan pesannya karena Firebase mengirimkan Tautan (Link), bukan Kode.
        tvSubtitle.text = "Kami mengirimkan tautan reset ke\n$email"

        btnContinue.setOnClickListener {
            // Karena password diubah di web Firebase, kita arahkan pengguna langsung
            // ke halaman Sukses atau kembali ke Login setelah mereka mengklik Continue.
            val intent = Intent(this, PasswordSuccessActivity::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            // Kembali ke Login (membersihkan tumpukan activity)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}