package com.example.kaskelasapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.auth.RoleSelectionActivity
import com.example.kaskelasapp.utils.BackgroundHelper
import com.google.android.material.button.MaterialButton

class ProfileSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackProfileSiswa)
        val btnLogoutSiswa = findViewById<MaterialButton>(R.id.btnLogoutSiswa)

        btnBack.setOnClickListener {
            finish()
        }

        btnLogoutSiswa.setOnClickListener {
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
