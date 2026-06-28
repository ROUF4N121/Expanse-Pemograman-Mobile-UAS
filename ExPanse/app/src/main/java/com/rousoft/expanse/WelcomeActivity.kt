package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Sembunyikan action bar
        supportActionBar?.hide()

        // Inisialisasi tombol
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Aksi ketika tombol Create Account diklik
        btnCreateAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Aksi ketika tombol Log In diklik
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}