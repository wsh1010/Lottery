package com.ggami.lottery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import com.ggami.lottery.databinding.ActivitySplashBinding
import render.animations.Fade
import render.animations.Render

class SplashActivity : AppCompatActivity() {
    private var mBinding: ActivitySplashBinding? = null
    private val binding get() = mBinding!!

    private val splashRunable: Runnable = Runnable {
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val render = Render(this)
        render.setAnimation(Fade().In(binding.imageSplash))
        render.start()

        Handler().postDelayed(splashRunable, 3000L)
    }
}