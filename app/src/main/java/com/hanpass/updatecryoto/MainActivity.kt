package com.hanpass.updatecryoto

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanpass.updatecryoto.databinding.ActivityMainBinding
import com.hanpass.updatecryoto.model.CgCoinListModel
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val gson = Gson()

    private lateinit var gcCoinMap: Map<String, String>
    private lateinit var exchangeCoinList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getMarketList()
        initListener()
    }

    private fun initListener() {
        binding.btnCoinCapUpdate.setOnClickListener {
            gcCoinMap = loadNameToIdMap()
            getMarketList()
        }

        binding.btnCoinInfoUpdate.setOnClickListener {
            getMarketList()
        }

        binding.btnCoinListUpdate.setOnClickListener {
            getGCMarketList()
        }
    }

    private fun getMarketList() {
        lifecycleScope.launch {
            val upbitServiceRes = Retrofits.upbitInstance.getMarketCodeList()
            val bithumbServiceRes = Retrofits.bithumbInstance.fetchBitThumbMarketCodeList()

            gcCoinMap = loadNameToIdMap()

            if (upbitServiceRes.isSuccessful && bithumbServiceRes.isSuccessful) {
                val upbitMarketCodeList = upbitServiceRes.body()
                val bithumbMarketCodeList = bithumbServiceRes.body()

                if (upbitMarketCodeList != null && bithumbMarketCodeList != null) {
                    exchangeCoinList =
                        (upbitMarketCodeList.map { it.englishName } + bithumbMarketCodeList.map {
                            it.englishName
                        }).toSet().toList()

                    Log.e("list", exchangeCoinList.toString())


                }
            }
        }
    }

    private fun getGCMarketList() {
        lifecycleScope.launch {
            try {
                val cgCoins = Retrofits.gcInstance.getCodeList()
                val jsonString = gson.toJson(cgCoins)

                val file = File(this@MainActivity.filesDir, "cgCoins.json")
                file.writeText(jsonString) // 내부 저장소에 저장

                Log.d("CoinSave", "성공적으로 저장됨: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("CoinSave", "저장 실패", e)
            }
        }
    }

    private fun loadNameToIdMap(): Map<String, String> {
        val file = File(this.filesDir, "cgCoins.json")
        if (!file.exists()) {
            Toast.makeText(this@MainActivity, "코인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return emptyMap()
        }

        val jsonString = file.readText()

        val gson = Gson()
        val type = object : TypeToken<List<CgCoinListModel>>() {}.type
        val coinList: List<CgCoinListModel> = gson.fromJson(jsonString, type)

        return coinList.associate { it.name to it.id }
    }
}