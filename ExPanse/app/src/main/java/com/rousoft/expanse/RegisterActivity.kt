package com.rousoft.expanse // Sesuaikan dengan package-mu

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth // IMPORT FIREBASE AUTH

class RegisterActivity : AppCompatActivity() {

    // Deklarasikan variabel FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val tvLoginNow = findViewById<TextView>(R.id.tvLoginNow)
        val btnSignUp = findViewById<MaterialButton>(R.id.btnSignUp)

        // 1. Inisialisasi kolom input teks (Pastikan ID ini sama persis dengan di XML kamu)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        // Pindah ke halaman Login jika sudah punya akun
        tvLoginNow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Tutup halaman register
        }

        // Aksi ketika tombol Sign Up ditekan
        btnSignUp.setOnClickListener {
            // Ambil teks yang diketik user
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 2. Validasi Input Kosong
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Validasi Konfirmasi Password
            if (password != confirmPassword) {
                Toast.makeText(this, "Password dan Konfirmasi Password tidak sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Validasi Panjang Password (Aturan baku Firebase)
            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 5. Daftarkan ke server Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Jika Register sukses
                        Toast.makeText(this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show()

                        // Karena berhasil mendaftar, otomatiskan login dan pindah ke halaman selanjutnya
                        val intent = Intent(this@RegisterActivity, WelcomeBackActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Jika Register gagal (contoh: email sudah pernah dipakai, format email salah)
                        val errorMessage = task.exception?.message ?: "Terjadi kesalahan"
                        Toast.makeText(this, "Gagal Mendaftar: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}