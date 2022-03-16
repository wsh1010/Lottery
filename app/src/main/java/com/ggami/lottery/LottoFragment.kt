package com.ggami.lottery

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginTop
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieAnimationView
import com.ggami.lottery.databinding.FragmentLottoBinding
import kotlinx.coroutines.*
import render.animations.Fade
import render.animations.Render
import retrofit2.Call
import retrofit2.Response
import java.sql.Date
import java.text.SimpleDateFormat
import javax.security.auth.callback.Callback
import kotlin.concurrent.timer
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LottoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LottoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mBinding: FragmentLottoBinding? = null
    private val binding get() = mBinding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var selectBalls: MutableMap<Int, MutableList<TextView>>
    private var round = 1005
    private var loadingEnd = false
    private var isStart = false

    var checkRound = mutableListOf<CheckBox>()
    var selectItems = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentLottoBinding.inflate(inflater, container, false)
        //return inflater.inflate(R.layout.fragment_lotto, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity = context as MainActivity
    }

    override fun onStart() {
        super.onStart()

        init()

        LottoData.readFileSettings(mainActivity)
        val isReadLottoInfo = LottoData.readFileInfo(mainActivity)

        //val isReadLottoInfo = false
        if (isReadLottoInfo) {
            if (LottoData.lottoInfo.isSelectNum) {
                round = LottoData.lottoInfo.round
                readLottoInfo()
            }
            CoroutineScope(Dispatchers.Default).launch {
                while (true) {
                    val call = LottoClient.LottoHttpClient.LottoService.getLottoNumbers(round)
                    var result = call.execute()
                    if (result.code() == 200) {
                        var resultInfo = result.body() as LottoClient.LottoResult
                        if (resultInfo.returnValue == "success") {
                            LottoData.setLottoRound(round + 1)
                            round = LottoData.lottoInfo.round
                            if (LottoData.lottoInfo.isSelectNum) {
                                LottoData.setLottoIsSelect(false)
                                LottoData.setLottoIsResult(false)
                            }
                        } else {
                            loadingEnd = true
                            break
                        }
                    }
                    delay(1000L)
                }
            }
        } else {
            //1. 회수 얻어오기.
            getRound()
            //
            LottoData.setLottoIsSelect(false)
            LottoData.setLottoIsResult(false)
            LottoData.setLottoRound(round)
        }

        timer(period = 1000L) {
            if (loadingEnd) {
                cancel()
                loadingEnd = false
                CoroutineScope(Dispatchers.Main).launch {
                    binding.loading.visibility = View.GONE
                    binding.completeLoading.visibility = View.VISIBLE
                    if (LottoData.lottoInfo.isSelectNum) {
                        binding.gameStartButton.visibility = View.GONE
                        binding.ballScreen.visibility = View.VISIBLE
                    } else {
                        binding.gameStartButton.visibility = View.VISIBLE
                    }
                    round = LottoData.lottoInfo.round
                    binding.dueDate.text =
                        "${LottoData.lottoInfo.round}회 (${LottoData.lottoInfo.dueDate} 발표 예정)"
                }
                LottoData.saveFileInfo(mainActivity)
            }
        }

        val resultAni = ValueAnimator.ofFloat(0f, 1f).setDuration(3000)
        resultAni.addUpdateListener {
            binding.gameStartButton.progress = it.animatedValue as Float
        }
        binding.gameStartButton.setOnClickListener {
            if (!isStart) {
                binding.gameStartButton.playAnimation()
                isStart = false
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000L)
                    randomSelectBalls(true, round)
                    delay(1000L)
                    binding.gameStartButton.visibility = View.GONE
                    binding.ballScreen.visibility = View.VISIBLE
                    ballScreenAnimation(true)
                }
            }
        }
        binding.settingsButton.setOnClickListener {
            //randomSelectBalls()
            var intent = Intent(mainActivity, LottoSettingsActivity::class.java)
            startActivity(intent)
        }

        binding.drawBallButton.setOnClickListener {
            val positiveButton = { _: DialogInterface, _: Int ->
                selectItems.clear()
                for ((i, content) in checkRound.withIndex()) {
                    if (content.isChecked) {
                        selectItems.add(i + 1)
                    }
                }
                randomSelectBalls(false, round)
                ballScreenAnimation(false)
                Toast.makeText(mainActivity, selectItems.toString(), Toast.LENGTH_SHORT).show()
            }
            var layout = createDialogView()
            var builder = AlertDialog.Builder(mainActivity)
            builder.setTitle("바꾸고싶은 라운드")
            builder.setView(layout)
            builder.setPositiveButton("확인", positiveButton)
            builder.setNegativeButton("취소", null)

            builder.show()

        }
    }

    private fun ballScreenAnimation(isAll: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            mainActivity.window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            selectBalls.forEach { (t, u) ->
                if (!isAll) {
                    if (!selectItems.contains((t))) return@forEach
                }
                for ((i, content) in u.withIndex()) {
                    var render = Render(mainActivity)
                    render.setAnimation(Fade().In(content))
                    render.start()
                    delay(300L)
                    content.visibility = View.VISIBLE
                    content.setBackgroundResource(getBallColor(LottoData.lottoInfo.roundAndSelectBalls[round]!![(((t-1) * 6) + i)]))
                    content.text =
                        LottoData.lottoInfo.roundAndSelectBalls[round]!![(((t-1) * 6) + i)].toString()
                }
            }
            mainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

    }

    private fun createDialogView(): LinearLayout {
        var dialogView = LinearLayout(mainActivity)
        dialogView.orientation = LinearLayout.VERTICAL

        var allLayout = LinearLayout(mainActivity)
        var roundLayout = LinearLayout(mainActivity)

        var layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, changeDP(10), 0, 0)
        allLayout.layoutParams = layoutParams
        roundLayout.layoutParams = layoutParams
        allLayout.orientation = LinearLayout.HORIZONTAL
        roundLayout.orientation = LinearLayout.HORIZONTAL

        dialogView.addView(allLayout)
        dialogView.addView(roundLayout)

        var checkAll = CheckBox(mainActivity)
        checkAll.text = "All"
        checkRound.clear()


        allLayout.addView(checkAll)

        for (i in 1..5) {
            var box = CheckBox(mainActivity)
            box.text = "${i}R"
            checkRound.add(box)
            roundLayout.addView(box)
        }
        checkAll.setOnClickListener {
            (it as CheckBox)
            if (!it.isChecked) {
                for (view in checkRound) {
                    if (view.isChecked) {
                        view.isChecked = false
                    }
                }
            } else {
                for (view in checkRound) {
                    if (!view.isChecked) {
                        view.isChecked = true
                    }
                }
            }
        }
        return dialogView
    }

    private fun init() {
        // 1 set balls
        selectBalls = mutableMapOf()

        selectBalls[1] = mutableListOf()
        selectBalls[1]!!.add(binding.select1Ball1)
        selectBalls[1]!!.add(binding.select1Ball2)
        selectBalls[1]!!.add(binding.select1Ball3)
        selectBalls[1]!!.add(binding.select1Ball4)
        selectBalls[1]!!.add(binding.select1Ball5)
        selectBalls[1]!!.add(binding.select1Ball6)

        selectBalls[2] = mutableListOf()
        selectBalls[2]!!.add(binding.select2Ball1)
        selectBalls[2]!!.add(binding.select2Ball2)
        selectBalls[2]!!.add(binding.select2Ball3)
        selectBalls[2]!!.add(binding.select2Ball4)
        selectBalls[2]!!.add(binding.select2Ball5)
        selectBalls[2]!!.add(binding.select2Ball6)

        selectBalls[3] = mutableListOf()
        selectBalls[3]!!.add(binding.select3Ball1)
        selectBalls[3]!!.add(binding.select3Ball2)
        selectBalls[3]!!.add(binding.select3Ball3)
        selectBalls[3]!!.add(binding.select3Ball4)
        selectBalls[3]!!.add(binding.select3Ball5)
        selectBalls[3]!!.add(binding.select3Ball6)

        selectBalls[4] = mutableListOf()
        selectBalls[4]!!.add(binding.select4Ball1)
        selectBalls[4]!!.add(binding.select4Ball2)
        selectBalls[4]!!.add(binding.select4Ball3)
        selectBalls[4]!!.add(binding.select4Ball4)
        selectBalls[4]!!.add(binding.select4Ball5)
        selectBalls[4]!!.add(binding.select4Ball6)

        selectBalls[5] = mutableListOf()
        selectBalls[5]!!.add(binding.select5Ball1)
        selectBalls[5]!!.add(binding.select5Ball2)
        selectBalls[5]!!.add(binding.select5Ball3)
        selectBalls[5]!!.add(binding.select5Ball4)
        selectBalls[5]!!.add(binding.select5Ball5)
        selectBalls[5]!!.add(binding.select5Ball6)

        binding.loading.visibility = View.VISIBLE
        binding.completeLoading.visibility = View.GONE
        binding.ballScreen.visibility = View.GONE
        binding.gameStartButton.visibility = View.GONE
        selectBalls.forEach { (t, u) ->
            for ((_, content) in u.withIndex()) {
                content.visibility = View.INVISIBLE
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

    private fun randomSelectBalls(isAll: Boolean, curr_round: Int) {
        if (isAll) {
            LottoData.lottoInfo.roundAndSelectBalls[curr_round] = mutableListOf()
        }
        selectBalls.forEach { (t, u) ->
            var lottery = Lottery()
            var fixedNums = LottoData.lottoSetting.fixedBall
            var nums = lottery.getNumber(fixedNums)
            if (!isAll) {
                if (!selectItems.contains(t)) {
                    return@forEach
                } else {
                    for (i in 0 until u.size) {
                        LottoData.lottoInfo.roundAndSelectBalls[curr_round]!![((t-1) * 6) + i] = nums[i]
                    }
                }
            } else {
                LottoData.lottoInfo.roundAndSelectBalls[curr_round]!!.addAll(nums)
            }


        }
        LottoData.setLottoIsSelect(true)
        LottoData.saveFileInfo(mainActivity)
    }

    private fun readLottoInfo() {
        var lottoballs = LottoData.lottoInfo.roundAndSelectBalls

        if (lottoballs != null) {
            selectBalls.forEach { (t, u) ->
                for ((i, content) in u.withIndex()) {
                    content.setBackgroundResource(getBallColor(lottoballs[round]!![(((t-1) * 6) + i)]))
                    content.text = lottoballs[round]!![(((t-1) * 6) + i)].toString()
                    content.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getRound() {
        CoroutineScope(Dispatchers.Default).launch {
            var preRound = 0
            var preCheck = false
            while (true) {
                try {
                    val call = LottoClient.LottoHttpClient.LottoService.getLottoNumbers(round)
                    var result = call.execute()
                    if (result.code() == 200) {
                        var value = result.body() as LottoClient.LottoResult
                        if (value.returnValue == "success") {
                            if (preCheck) {
                                round = preRound
                                LottoData.lottoInfo.round = round
                                loadingEnd = true
                                break
                            } else {
                                LottoData.calDueDate()
                                var gap =
                                    (Date.valueOf(LottoData.lottoInfo.dueDate).time - value.drwNoDate.time) / (24 * 60 * 60 * 1000 * 7)
                                preRound = round
                                round += gap.toInt()
                            }
                        } else {
                            if (preRound == round - 1) {
                                LottoData.lottoInfo.round = round
                                loadingEnd = true
                                break
                            } else {
                                preRound = round
                                round -= 1
                                preCheck = true
                            }
                        }

                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                mainActivity,
                                "${result.code()}인터넷 연결을 확인해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        break
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(mainActivity, "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                        Log.d("Error", e.message!!)
                    }
                    break
                }
            }
        }
    }

    private fun changeDP(value: Int): Int {
        var displayMetrics = resources.displayMetrics
        return (value * displayMetrics.density).roundToInt()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LottoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LottoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}