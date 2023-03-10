package com.example.lottogenerator

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lottogenerator.databinding.ActivityMainBinding
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.generatebutton.setOnClickListener {
            val lottoNumbers = createLottoNumbers()
            Log.d("TAG", lottoNumbers.toString())
            updateLottoBallImage(lottoNumbers)

            CoroutineScope(Dispatchers.IO + job).launch {
                val winningNumbers = async { getLottoNumbers() }
                val rank = whatIsRank(lottoNumbers, winningNumbers.await())
                val text = "${winningNumbers.await()} : $rank"

                withContext(Dispatchers.Main) {
                    binding.tvWinning.text = text
                }
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun createLottoNumbers(): ArrayList<Int> {
        val result = arrayListOf<Int>()

        val source = IntArray(45) { it + 1 }
        val seed =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.KOREA).format(Date()).hashCode()
                .toLong()
        val random = Random(seed)
        source.shuffle(random)
        source.slice(0..5).forEach { num ->
            result.add(num)
        }
        result.sort()

        var evenNumberCount = 0
        var oddNumberCount = 0
        for (num in result) {
            if (num % 2 == 0) {
                evenNumberCount += 1
            } else {
                oddNumberCount += 1
            }
        }
        result.add(result.sum())
        result.add(oddNumberCount)
        result.add(evenNumberCount)

        return result
    }

    private fun getDrawableID(number: Int): Int {
        val number = String.format("%02d", number)
        val string = "ball_$number"
        val id = resources.getIdentifier(string, "drawable", packageName)
        return id
    }

    private fun updateLottoBallImage(result: ArrayList<Int>) {
        with(binding) {
            ivGame0.setImageResource(getDrawableID(result[0]))
            ivGame1.setImageResource(getDrawableID(result[1]))
            ivGame2.setImageResource(getDrawableID(result[2]))
            ivGame3.setImageResource(getDrawableID(result[3]))
            ivGame4.setImageResource(getDrawableID(result[4]))
            ivGame5.setImageResource(getDrawableID(result[5]))
            tvAnalyze.text = "?????????: ${result[6]}  ???:???=${result[7]}:${result[8]}"
        }
    }

    private suspend fun getLottoNumbers(): ArrayList<Int> {
        val num = Integer.parseInt(binding.roundNum.text.toString())
//        val round = "970" //??????
        if (0<num || num<1000){

        }

        val url =
            "https://dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=$num" // Get????????? gson ????????? ??????
        val lottoNumbers = ArrayList<Int>() // ?????? ?????? ?????? ?????????

        try {
            val response = URL(url).readText()
            val jsonObject = JsonParser.parseString(response).asJsonObject
            val returnValue = jsonObject.get("returnValue").asString
            if (returnValue == "success") {
                for (i in 1..6) {
                    val lottoNumber = jsonObject.get("drwtNo$i").asInt
                    lottoNumbers.add(lottoNumber)
                }
                val bonusNumber = jsonObject.get("bnusNo").asInt
                lottoNumbers.add(bonusNumber)
                 lottoNumbers.add(num)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lottoNumbers
    }

    private fun whatIsRank(lottoNumbers:ArrayList<Int>, winningNumbers:ArrayList<Int>):String{
        var matchCount = 0
        var answer = ""
        if (winningNumbers.size == 0){
            answer="?????? ??????"
            return answer
        }else{
            for (i in 0..5){
                if (lottoNumbers.contains(winningNumbers[i])) matchCount += 1
            }
            if (matchCount == 6) {
                answer = "1???"
            } else if (matchCount == 5) {
                if (lottoNumbers.contains(winningNumbers[6])){
                    answer = "2???"
                }
                else{
                    answer = "3???"
                }
            } else if (matchCount == 4){
                answer = "4???"
            } else if (matchCount == 4){
                answer = "5???"
            } else {
                answer = "??????"
            }
            return answer
        }
    }
}