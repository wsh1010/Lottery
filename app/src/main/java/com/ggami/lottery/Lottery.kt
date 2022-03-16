package com.ggami.lottery

import android.util.Log
import java.util.*

class Lottery {
    private var lottonums = MutableList<Int>(45) {i->i+1}


    fun getNumber(selectNum : List<Int>) : List<Int> {
        var needNum = 6 - selectNum.size
        var resultNums = MutableList<Int>(0) { i -> i }

        for (i in selectNum.indices){
            resultNums.add(selectNum[i])
            lottonums.remove(selectNum[i])
        }
        lottonums.shuffle()
        var counts = Random().nextInt(10)
        for(count in 0 until counts) {
            for (i in 0..lottonums.size - 2) {
                Log.d("test", (lottonums.size - i).toString())
                var j = Random().nextInt((lottonums.size - i)) + i
                var temp = lottonums[i]
                lottonums[i] = lottonums[j]
                lottonums[j] = temp
            }
        }
        for (i in 0 until needNum){
            resultNums.add(lottonums[i])
        }

        resultNums.sort()
        return resultNums
    }
}

