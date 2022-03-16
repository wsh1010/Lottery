package com.ggami.lottery

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LottoData {
    data class LottoInfo(
        var round : Int = 0,
        var isSelectNum : Boolean = false,
        var isResult : Boolean = false,
        var roundAndSelectBalls : MutableMap<Int, MutableList<Int>> = mutableMapOf(),
        var dueDate: String =  ""
    )
    data class LottoSetting(
        var fixedBall: MutableList<Int> = mutableListOf()
    )
    companion object {
        var lottoInfo = LottoInfo()
        var lottoSetting = LottoSetting()
        private const val fileInfoName = "LottoInfo"
        private const val fileSettingName = "LottoSettings"
        fun readFileInfo(context: Context): Boolean {
            val file = File(context.cacheDir, fileInfoName)
            if(file.exists()) {
                lottoInfo = Gson().fromJson(file.readText(), LottoInfo::class.java)
                return true
            }
            return false
        }
        fun saveFileInfo(context: Context) {
            val fileContents = Gson().toJson(lottoInfo)
            val file = File(context.cacheDir, fileInfoName)
            file.writeText(fileContents)
        }
        fun readFileSettings(context: Context): Boolean {
            val file = File(context.cacheDir, fileSettingName)
            if(file.exists()) {
                lottoSetting = Gson().fromJson(file.readText(), LottoSetting::class.java)
                return true
            }
            return false
        }
        fun saveFileSettings(context: Context) {
            val fileContents = Gson().toJson(lottoSetting)
            val file = File(context.cacheDir, fileSettingName)
            file.writeText(fileContents)
        }
        fun setLottoRound(round : Int) {
            lottoInfo.round = round
        }
        fun setLottoIsSelect(isSelect : Boolean) {
            lottoInfo.isSelectNum = isSelect
        }
        fun setLottoIsResult(isResult : Boolean) {
            lottoInfo.isResult = isResult
        }
        fun getLottoInfoGson() : String{
            return Gson().toJson(lottoInfo)
        }

        fun calDueDate() : String {
            var formatter = SimpleDateFormat("yyyy-MM-dd")
            var calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, (7 - calendar.get(Calendar.DAY_OF_WEEK)))
            lottoInfo.dueDate = formatter.format(calendar.time)
            return formatter.format(calendar.time)
        }
    }
}