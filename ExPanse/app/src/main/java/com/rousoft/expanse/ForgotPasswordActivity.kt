package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // IMPORT FIREBASE AUTH

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        supportActionBar?.hide()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnBack = findViewById<Button>(R.id.btnBackToLogin)

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                // Tembak permintaan reset password ke Firebase
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email reset password telah dikirim!", Toast.LENGTH_SHORT).show()

                            // Pindah ke layar selanjutnya dan bawa data emailnya
                            val intent = Intent(this, PasswordResetActivity::class.java)
                            intent.putExtra("USER_EMAIL", email)
                            startActivity(intent)
                        } else {
                            val error = task.exception?.message ?: "Terjadi kesalahan"
                            Toast.makeText(this, "Gagal: $error", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                etEmail.error = "Silakan masukkan email Anda"
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}