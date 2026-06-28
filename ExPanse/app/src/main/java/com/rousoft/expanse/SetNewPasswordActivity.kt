package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SetNewPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_new_password)
        supportActionBar?.hide()

        val btnReset = findViewById<Button>(R.id.btnResetPassword)

        btnReset.setOnClickListener {
            // Nanti di sini ada logika update password di Firebase
            val intent = Intent(this, PasswordSuccessActivity::class.java)
            startActivity(intent)
        }
    }
}