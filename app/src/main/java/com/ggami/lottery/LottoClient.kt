package com.ggami.lottery

import com.ggami.lottery.LottoClient.Companion.getLottoNumbers
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

class LottoClient {
    data class LottoResult(
        @SerializedName("returnValue") var returnValue: String,
        @SerializedName("drwtNo1") var drwtNo1: Int,
        @SerializedName("drwtNo2") var drwtNo2: Int,
        @SerializedName("drwtNo3") var drwtNo3: Int,
        @SerializedName("drwtNo4") var drwtNo4: Int,
        @SerializedName("drwtNo5") var drwtNo5: Int,
        @SerializedName("drwtNo6") var drwtNo6: Int,
        @SerializedName("bnusNo") var bnusNo: Int,
        @SerializedName("drwNoDate") var drwNoDate: Date
        )
    companion object{
        const val url = "https://www.dhlottery.co.kr/"

        fun getLottoNumbers(round: Int) {
            val call = LottoHttpClient.LottoService.getLottoNumbers(round)
            call.enqueue(object: Callback<LottoResult> {
                override fun onResponse(call: Call<LottoResult>, response: Response<LottoResult>) {
                }

                override fun onFailure(call: Call<LottoResult>, t: Throwable) {
                }
            })
        }

    }

    object LottoHttpClient {
        private val Client: Retrofit.Builder by lazy {
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
        }
        val LottoService: LottoApi by lazy {
            Client.build().create(LottoApi::class.java)
        }
    }
    interface LottoApi{
        //https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=861
        @GET("common.do")
        fun getLottoNumbers(@Query("drwNo") drwNo: Int, @Query("method") method: String = "getLottoNumber") : Call<LottoResult>
    }
}