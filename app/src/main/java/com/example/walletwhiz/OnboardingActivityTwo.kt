package com.example.walletwhiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivityTwo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_activity_two)

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }
    }
}
