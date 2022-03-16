package com.ggami.lottery

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.ggami.lottery.databinding.FragmentLottoBinding
import com.ggami.lottery.databinding.FragmentResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mainActivity: MainActivity
    private var mBinding: FragmentResultBinding? = null
    private val binding get() = mBinding!!

    private var lastRound = 0
    private lateinit var lastBalls: MutableMap<Int, MutableList<TextView>>
    private var lastResult  = SingleLiveEvent<LottoClient.LottoResult>()
    private val getLastResult  : LiveData<LottoClient.LottoResult>
            get() = lastResult

    private var resultBalls = mutableListOf<Int>()
    private var bonusBall = 0

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
       // return inflater.inflate(R.layout.fragment_result, container, false)
        mBinding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity = context as MainActivity
    }

    override fun onStart() {
        super.onStart()

        initBall()
        lastRound = LottoData.lottoInfo.round - 1


        checkResult()
        sendLottoResult(lastRound)
    }

    private fun initBall(){
        lastBalls = mutableMapOf()

        lastBalls[1] = mutableListOf()
        lastBalls[1]!!.add(binding.lastballs11)
        lastBalls[1]!!.add(binding.lastballs12)
        lastBalls[1]!!.add(binding.lastballs13)
        lastBalls[1]!!.add(binding.lastballs14)
        lastBalls[1]!!.add(binding.lastballs15)
        lastBalls[1]!!.add(binding.lastballs16)

        lastBalls[2] = mutableListOf()
        lastBalls[2]!!.add(binding.lastballs21)
        lastBalls[2]!!.add(binding.lastballs22)
        lastBalls[2]!!.add(binding.lastballs23)
        lastBalls[2]!!.add(binding.lastballs24)
        lastBalls[2]!!.add(binding.lastballs25)
        lastBalls[2]!!.add(binding.lastballs26)

        lastBalls[3] = mutableListOf()
        lastBalls[3]!!.add(binding.lastballs31)
        lastBalls[3]!!.add(binding.lastballs32)
        lastBalls[3]!!.add(binding.lastballs33)
        lastBalls[3]!!.add(binding.lastballs34)
        lastBalls[3]!!.add(binding.lastballs35)
        lastBalls[3]!!.add(binding.lastballs36)

        lastBalls[4] = mutableListOf()
        lastBalls[4]!!.add(binding.lastballs41)
        lastBalls[4]!!.add(binding.lastballs42)
        lastBalls[4]!!.add(binding.lastballs43)
        lastBalls[4]!!.add(binding.lastballs44)
        lastBalls[4]!!.add(binding.lastballs45)
        lastBalls[4]!!.add(binding.lastballs46)

        lastBalls[5] = mutableListOf()
        lastBalls[5]!!.add(binding.lastballs51)
        lastBalls[5]!!.add(binding.lastballs52)
        lastBalls[5]!!.add(binding.lastballs53)
        lastBalls[5]!!.add(binding.lastballs54)
        lastBalls[5]!!.add(binding.lastballs55)
        lastBalls[5]!!.add(binding.lastballs56)
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

    private fun sendLottoResult(getRound : Int ){
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val client = LottoClient.LottoHttpClient.LottoService.getLottoNumbers(getRound)
                var result = client.execute()
                if (result.code() == 200) {
                    CoroutineScope(Dispatchers.Main).launch {
                        lastResult.value = result.body() as LottoClient.LottoResult
                    }
                }
            }catch(e: Exception) {

            }
        }
    }

    private fun checkResult() {
        var lastmyballs = List(30, {i -> i})
        var getCoins = 0
        getLastResult.observe(this, {
            binding.resultTitle.text = "당첨번호 $lastRound"
            if ( it.returnValue == "success") {
                binding.resultball1.text = it.drwtNo1.toString()
                binding.resultball1.setBackgroundResource(getBallColor(it.drwtNo1))
                binding.resultball2.text = it.drwtNo2.toString()
                binding.resultball2.setBackgroundResource(getBallColor(it.drwtNo2))
                binding.resultball3.text = it.drwtNo3.toString()
                binding.resultball3.setBackgroundResource(getBallColor(it.drwtNo3))
                binding.resultball4.text = it.drwtNo4.toString()
                binding.resultball4.setBackgroundResource(getBallColor(it.drwtNo4))
                binding.resultball5.text = it.drwtNo5.toString()
                binding.resultball5.setBackgroundResource(getBallColor(it.drwtNo5))
                binding.resultball6.text = it.drwtNo6.toString()
                binding.resultball6.setBackgroundResource(getBallColor(it.drwtNo6))
                resultBalls.add(it.drwtNo1)
                resultBalls.add(it.drwtNo2)
                resultBalls.add(it.drwtNo3)
                resultBalls.add(it.drwtNo4)
                resultBalls.add(it.drwtNo5)
                resultBalls.add(it.drwtNo6)
                binding.resultballBonus.text = it.bnusNo.toString()
                binding.resultballBonus.setBackgroundResource(getBallColor(it.bnusNo))
                bonusBall = it.bnusNo

                //if(LottoData.lottoInfo.roundAndSelectBalls[lastRound] == null){
                if(lastmyballs == null){
                    binding.resultEmpty.visibility=View.VISIBLE
                }else {
                    lastBalls.forEach { (t, contents) ->
                        for ((index, content) in contents.withIndex()) {
                            var num = lastmyballs[((t - 1) * 6 + index)]
                            content.text = num.toString()
                            if (resultBalls.contains(num)) {
                                content.setBackgroundResource(getBallColor(num))
                                content.setTextColor(Color.WHITE)
                                getCoins += 10
                            }
                        }
                    }
                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ResultFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}