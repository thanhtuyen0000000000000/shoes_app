package com.example.shoes.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.example.shoes.databinding.ActivityIntroBinding


class IntroActivity : BaseActivity() {
    private lateinit var binding:ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.startBtn.setOnClickListener {
//            startActivity(Intent(this@IntroActivity, MainActivity::class.java))
//        }
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this@IntroActivity, RegisterActivity::class.java))
        }
        // Thiết lập text clickable cho "Sign In"
        val fullText = "Already have an account? Sign In"
        val spannable = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Chuyển sang LoginActivity (thay tên class nếu bạn đặt khác)
                startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#6033ba") // Màu của chữ "Sign In"
                ds.isUnderlineText = false // Không gạch dưới
            }
        }

        val start = fullText.indexOf("Sign In")
        val end = start + "Sign In".length
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.textView3.text = spannable
        binding.textView3.movementMethod = LinkMovementMethod.getInstance()
        binding.textView3.highlightColor = Color.TRANSPARENT
    }
}