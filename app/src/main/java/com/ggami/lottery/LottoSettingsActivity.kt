package com.ggami.lottery

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Dimension
import com.airbnb.lottie.LottieAnimationView
import com.ggami.lottery.databinding.ActivityLottoSettingsBinding
import org.w3c.dom.Text
import kotlin.math.roundToInt

class LottoSettingsActivity : AppCompatActivity() {
    private var mBinding: ActivityLottoSettingsBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_lotto_settings)
        mBinding = ActivityLottoSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = "Lotto Settings"

        settingsBall()


        binding.saveButton.setOnClickListener {
            LottoData.saveFileSettings(this)
            finish()
        }
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home-> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun settingsBall() {
        drawFixedBall()
        var vertical = binding.verticalLine
        var horizontal = binding.horizontalLine
        var ball = binding.selectNum

        for(i in 1..45) {
            if (((i-1) % 9) == 0) {
                horizontal = LinearLayout(this)
                horizontal.layoutParams = binding.horizontalLine.layoutParams
                horizontal.gravity = Gravity.CENTER
                horizontal.orientation = binding.horizontalLine.orientation
                vertical.addView(horizontal)
            }
            var insertBall = TextView(this)
            insertBall.layoutParams = ball.layoutParams
            insertBall.text = i.toString()
            insertBall.setTextColor(ball.textColors)
            insertBall.setTextSize(Dimension.SP, 20.0f)
            insertBall.gravity = ball.gravity
            insertBall.typeface = ball.typeface
            insertBall.setBackgroundResource(getBallColor(i))
            horizontal.addView(insertBall)
            insertBall.setOnClickListener{
                if (LottoData.lottoSetting.fixedBall.contains(i)) {
                    //LottoData.lottoSetting.fixedBall.remove(i)
                } else {
                    if(LottoData.lottoSetting.fixedBall.size < 5) {
                        LottoData.lottoSetting.fixedBall.add(i)
                    } else {
                        Toast.makeText(this, "5개까지 설정이 가능합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                drawFixedBall()
            }
        }
    }

    private fun getBallColor(num: Int): Int {
        return when (num) {
            in 1..10 -> {
                R.drawable.yellowball
            }
            in 11..20 -> {
                R.drawable.blueball
            }
            in 21..30 -> {
                R.drawable.redball
            }
            in 31..40 -> {
                R.drawable.blackball
            }
            in 41..45 -> {
                R.drawable.greenball
            }
            else -> 0
        }
    }
    private fun changeDP(value : Int) : Int {
        var displayMetrics = resources.displayMetrics
        return (value * displayMetrics.density).roundToInt()
    }

    private fun drawFixedBall() {
        binding.linearLayout.removeAllViews()
        if (LottoData.lottoSetting.fixedBall.size == 0) {
            var empty = TextView(this)
            empty.layoutParams = binding.fixedNum.layoutParams
            empty.text = "없음"
            empty.setTextSize(Dimension.SP, 25.0f)
            empty.gravity = Gravity.CENTER
            empty.typeface = binding.fixedNum.typeface
            binding.linearLayout.addView(empty)
        }else {
            binding.fixedNum.visibility = View.GONE
            for (i in LottoData.lottoSetting.fixedBall) {

                var insertBall = TextView(this)
                insertBall.layoutParams = binding.fixedNum.layoutParams
                insertBall.text = i.toString()
                insertBall.setTextColor(Color.WHITE)
                insertBall.setTextSize(Dimension.SP, 25.0f)
                insertBall.gravity = Gravity.CENTER
                insertBall.typeface = binding.fixedNum.typeface
                insertBall.setBackgroundResource(getBallColor(i))
                insertBall.setOnClickListener{
                    LottoData.lottoSetting.fixedBall.remove(i)
                    drawFixedBall()
                }
                binding.linearLayout.addView(insertBall)
            }
        }
    }
}