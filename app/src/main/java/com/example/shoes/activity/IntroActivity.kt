package com.example.shoes.activity

import android.content.Intent
import android.os.Bundle
import com.example.shoes.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Get Started button - chuyển đến RegisterActivity
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Sign in text - chuyển đến LoginActivity
        binding.signinText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}