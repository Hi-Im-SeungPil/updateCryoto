package com.hanpass.updatecryoto

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanpass.updatecryoto.model.CgCoinListModel
import com.hanpass.updatecryoto.model.NeedCoinInfoData
import com.hanpass.updatecryoto.model.NeedMarketData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jeonfeel.moeuibit2.databinding.ActivityMainBinding
import java.io.File

//genesis_date
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gson = Gson()

    private lateinit var gcCoinMap: Map<String, String>
    private lateinit var exchangeCoinList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        initListener()
    }

    private fun initListener() {
        binding.btnCoinCapUpdate.setOnClickListener {
            lifecycleScope.launch {
                gcCoinMap = loadNameToIdMap()
                getMarketList()
                coinCapUpdate()
            }
        }

        binding.btnCoinInfoUpdate.setOnClickListener {
            lifecycleScope.launch {
                gcCoinMap = loadNameToIdMap()
                getMarketList()
                coinInfoUpdate()
            }
        }

        binding.btnCoinListUpdate.setOnClickListener {
            lifecycleScope.launch {
                getGCMarketList()
            }
        }
    }

    private suspend fun getMarketList() {
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

    private suspend fun getGCMarketList() {
        try {
            val cgCoins = Retrofits.gcInstance.getCodeList().body()

            val file = File(this@MainActivity.filesDir, "cgCoins.json")
            file.writeText(cgCoins.toString()) // 내부 저장소에 저장

            Log.d("CoinSave", "성공적으로 저장됨: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CoinSave", "저장 실패", e)
        }
    }


    private suspend fun loadNameToIdMap(): Map<String, String> {
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

    private suspend fun coinCapUpdate() {
        val resultList = mutableListOf<NeedMarketData>()
        val idList = exchangeCoinList.mapNotNull { name ->
            gcCoinMap[name]
        }

        val chunkedIds = idList.chunked(200)

        for (chunk in chunkedIds) {
            val idsParam = chunk.joinToString(",")
            val response = Retrofits.gcInstance.getMarketData(
                ids = idsParam
            )

            val list = response.body()?.map { it.parseNeedData() } ?: emptyList()
            resultList.addAll(list)
        }

        val updates =
            resultList.associateBy { coin -> "/coinInfoData/${coin.symbol.uppercase()}" }

        try {
            FirebaseDatabase.getInstance().reference.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("FirebaseUpdate", "Realtime DB 업데이트 성공")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseUpdate", "Realtime DB 업데이트 실패", e)
                }
        } catch (e: Exception) {
            Log.e("FirebaseUpdate", "예외 발생", e)
        }
    }

    private suspend fun coinInfoUpdate() {
        val resultList = mutableListOf<NeedCoinInfoData>()
        val idList = exchangeCoinList.mapNotNull { name ->
            gcCoinMap[name]
        }

        for ((index, id) in idList.withIndex()) {
            val response = Retrofits.gcInstance.getCoinInfoData(
                id = id
            ).body()?.parseNeedData()

            if (response != null) {
                resultList.add(response)
            }

            binding.tvStatus.text = "${index + 1} / ${idList.size}"
            delay(2010)
        }

        val updates =
            resultList.associateBy { coin -> "/coinInfoData/${coin.symbol!!.uppercase()}" }

        try {
            FirebaseDatabase.getInstance().reference.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("FirebaseUpdate", "Realtime DB 업데이트 성공")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseUpdate", "Realtime DB 업데이트 실패", e)
                }
        } catch (e: Exception) {
            Log.e("FirebaseUpdate", "예외 발생", e)
        }
    }
}