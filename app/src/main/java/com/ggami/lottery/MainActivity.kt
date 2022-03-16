package com.ggami.lottery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ggami.lottery.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //광고
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        var pagerAdapter = PagerFragmentStateAdapter(this)
        pagerAdapter.addFragment(LottoFragment())
        pagerAdapter.addFragment(ResultFragment())
        pagerAdapter.addFragment(ShopFragment())
        pagerAdapter.addFragment(SettingsFragment())

        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {tab, param ->
            when(param){
                0->tab.setIcon(R.drawable.ic_baseline_casino_24)
                1->tab.setIcon(R.drawable.ic_baseline_assessment_24)
                2->tab.setIcon(R.drawable.ic_baseline_store_24)
                3->tab.setIcon(R.drawable.ic_baseline_settings_applications_24)
            }
        }.attach()

        // 로딩 표기
        //1. 파일 읽어오기.
        //2. 파일이 없다면 기본세팅하여 초기화 저장하기.
        //3. ?
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}