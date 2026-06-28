package com.rousoft.expanse // Sesuaikan dengan package-mu

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth // IMPORT FIREBASE AUTH

class LoginActivity : AppCompatActivity() {

    // Deklarasikan variabel FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val tvSignUpNow = findViewById<TextView>(R.id.tvSignUpNow)
        val btnLoginSubmit = findViewById<MaterialButton>(R.id.btnLoginSubmit)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword) // TAMBAHAN: Deklarasi tombol Lupa Password
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        // ==========================================
        // LOGIKA TOMBOL FORGOT PASSWORD (DI UBAH)
        // ==========================================
        tvForgotPassword.setOnClickListener {
            // Arahkan ke halaman ForgotPasswordActivity
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        // ==========================================

        // Pindah ke halaman Register jika belum punya akun
        tvSignUpNow.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Aksi ketika tombol Login ditekan
        btnLoginSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // --- JALUR DUMMY SEMENTARA UNTUK TESTING ---
            if (email == "topan67@email.com" && password == "topan67") {
                val intent = Intent(this@LoginActivity, WelcomeBackActivity::class.java)
                startActivity(intent)
                finish()
                return@setOnClickListener
            }
            // -------------------------------------------

            // 1. Validasi Input (Pastikan tidak ada yang kosong)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Autentikasi dengan Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Jika Login sukses, pindah ke halaman selanjutnya
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, WelcomeBackActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Jika Login gagal (contoh: password salah / email tidak terdaftar)
                        val errorMessage = task.exception?.message ?: "Terjadi kesalahan"
                        Toast.makeText(this, "Login Gagal: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // ==========================================
    // AUTO-LOGIN (Melewati halaman login jika sudah pernah masuk)
    // ==========================================
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Pengguna sudah login, langsung lempar ke WelcomeBackActivity atau MainActivity
            val intent = Intent(this, WelcomeBackActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}