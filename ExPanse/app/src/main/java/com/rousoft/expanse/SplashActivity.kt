package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Sembunyikan Action Bar
        supportActionBar?.hide()

        val tvLoading = findViewById<TextView>(R.id.tvLoading)
        val handler = Handler(Looper.getMainLooper())
        var dotCount = 1

        // Membuat fungsi yang berjalan berulang kali untuk animasi titik
        val runnable = object : Runnable {
            override fun run() {
                // Atur jumlah titik (1 sampai 3)
                if (dotCount > 3) {
                    dotCount = 1
                }

                // Tambahkan titik sesuai jumlah dotCount
                val dots = ".".repeat(dotCount)
                tvLoading.text = "Loading$dots"

                dotCount++

                // Ulangi proses ini setiap 500 milidetik (0.5 detik)
                handler.postDelayed(this, 500)
            }
        }

        // Mulai animasinya
        handler.post(runnable)

        // Pindah ke WelcomeActivity setelah 3 detik (3000 milidetik)
        Handler(Looper.getMainLooper()).postDelayed({
            // Hentikan animasi agar tidak bocor di memori
            handler.removeCallbacks(runnable)

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish() // Tutup Splash Screen agar tidak bisa di-back
        }, 3000)
    }
}