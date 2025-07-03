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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val gson = Gson()

    private lateinit var cgCoinMap: Map<String, String>
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
                cgCoinMap = loadNameToIdMap()
                getMarketList()
                coinCapUpdate()
            }
        }

        binding.btnCoinInfoUpdate.setOnClickListener {
            lifecycleScope.launch {
                cgCoinMap = loadNameToIdMap()
                getMarketList()
                coinInfoUpdate()
            }
        }

        binding.btnCoinListUpdate.setOnClickListener {
            lifecycleScope.launch {
                getGCMarketList()
            }
        }

        binding.btnCoinInfoUpdateExcept.setOnClickListener {
            lifecycleScope.launch {
                cgCoinMap = loadNameToIdMap()
                getMarketList()
                coinInfoUpdateExcept()
            }
        }
    }

    private suspend fun getMarketList() {
        val upbitServiceRes = Retrofits.upbitInstance.getMarketCodeList()
        val bithumbServiceRes = Retrofits.bithumbInstance.fetchBitThumbMarketCodeList()

        cgCoinMap = loadNameToIdMap()

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
            cgCoinMap[name]
        }

        Log.e("idList", idList.toString())

        val chunkedIds = idList.chunked(100)

        for (chunk in chunkedIds) {
            val idsParam = chunk.joinToString(",")
            val response = Retrofits.gcInstance.getMarketData(
                ids = idsParam
            )

            val list = response.body()?.map { it.parseNeedData() } ?: emptyList()
            resultList.addAll(list)
        }

        val updates: Map<String, Any> = resultList.flatMap { coin ->
            val basePath = "/coinInfoData/${coin.symbol.uppercase()}"
            listOf(
                "$basePath/circulatingSupply" to coin.circulatingSupply,
                "$basePath/fullyDilutedValuation" to coin.fullyDilutedValuation,
                "$basePath/image" to coin.image,
                "$basePath/marketCap" to coin.marketCap,
                "$basePath/marketCapRank" to coin.marketCapRank,
                "$basePath/maxSupply" to coin.maxSupply,
                "$basePath/totalSupply" to coin.totalSupply,
                "$basePath/symbol" to coin.symbol,
            )
        }.toMap()

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
            cgCoinMap[name]
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
            resultList.associateBy { coin -> "/coinInfoData/${coin.symbol!!.uppercase()}/community" }

        try {
            FirebaseDatabase.getInstance().reference.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("FirebaseUpdate", "Realtime DB 업데이트 성공")

                    try {
                        // 로컬에 저장
                        val file = File(this.filesDir, "coinInfoData.json")
                        val jsonString = Gson().toJson(resultList)
                        file.writeText(jsonString)
                        Log.d("LocalSave", "로컬 파일 저장 완료: ${file.absolutePath}")
                    } catch (e: Exception) {
                        Log.e("LocalSave", "로컬 저장 실패", e)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseUpdate", "Realtime DB 업데이트 실패", e)
                }
        } catch (e: Exception) {
            Log.e("FirebaseUpdate", "예외 발생", e)
        }
    }

    private suspend fun coinInfoUpdateExcept() {
        val existingSymbols: Set<String> = try {
            val file = File(this.filesDir, "coinInfoData.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<NeedCoinInfoData>>() {}.type
                val savedList: List<NeedCoinInfoData> = Gson().fromJson(json, type)
                savedList.mapNotNull { it.symbol?.uppercase() }.toSet()
            } else emptySet()
        } catch (e: Exception) {
            Log.e("LocalLoad", "기존 파일 로딩 실패", e)
            emptySet()
        }

        // 2. 업데이트할 대상만 필터링 (기존 symbol에 없는 것만)
        val resultList = mutableListOf<NeedCoinInfoData>()
        val idList = exchangeCoinList.mapNotNull { name ->
            val symbol = name.uppercase()
            val id = cgCoinMap[name]
            if (id != null && symbol !in existingSymbols) {
                symbol to id
            } else null
        }

        for ((index, pair) in idList.withIndex()) {
            val (symbol, id) = pair

            val response = Retrofits.gcInstance.getCoinInfoData(id = id).body()?.parseNeedData()
            if (response != null) {
                resultList.add(response)
            }

            binding.tvStatus.text = "${index + 1} / ${idList.size}"
            delay(2010)  // CoinGecko rate limit
        }

        // 3. 새로 추가된 코인만 업데이트
        val updates =
            resultList.associateBy { coin -> "/coinInfoData/${coin.symbol!!.uppercase()}/community" }

        // 4. Firebase에 쓰고 로컬에 병합 저장
        FirebaseDatabase.getInstance().reference.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("FirebaseUpdate", "새 코인만 업데이트 완료")

                try {
                    val file = File(this.filesDir, "coinInfoData.json")
                    val oldList: MutableList<NeedCoinInfoData> = if (file.exists()) {
                        val oldJson = file.readText()
                        val type = object : TypeToken<List<NeedCoinInfoData>>() {}.type
                        Gson().fromJson(oldJson, type)
                    } else mutableListOf()

                    oldList.addAll(resultList)

                    val jsonString = Gson().toJson(oldList)
                    file.writeText(jsonString)
                    Log.d("LocalSave", "로컬 파일 병합 저장 완료")
                } catch (e: Exception) {
                    Log.e("LocalSave", "병합 저장 실패", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUpdate", "업데이트 실패", e)
            }
    }
}