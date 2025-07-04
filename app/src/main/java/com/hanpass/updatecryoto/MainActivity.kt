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

    private lateinit var cgEngNameCoinMap: MutableMap<String, String>
    private lateinit var cgSymbolCoinMap: Map<String, String>
    private lateinit var exchangeCoinList: List<String>
    private var exchangeSymbolCoinList: MutableList<String> = mutableListOf()

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
                val idPair = loadNameToIdMap()
                cgEngNameCoinMap = idPair.first
                cgSymbolCoinMap = idPair.second
                getMarketList()
                coinCapUpdate()
            }
        }

        binding.btnCoinInfoUpdate.setOnClickListener {
            lifecycleScope.launch {
                val idPair = loadNameToIdMap()
                cgEngNameCoinMap = idPair.first
                cgSymbolCoinMap = idPair.second
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
                val idPair = loadNameToIdMap()
                cgEngNameCoinMap = idPair.first
                cgSymbolCoinMap = idPair.second
                getMarketList()
//                coinInfoUpdateExcept()
            }
        }
    }

    private suspend fun getMarketList() {
        val upbitServiceRes = Retrofits.upbitInstance.getMarketCodeList()
        val bithumbServiceRes = Retrofits.bithumbInstance.fetchBitThumbMarketCodeList()

        if (upbitServiceRes.isSuccessful && bithumbServiceRes.isSuccessful) {
            val upbitMarketCodeList = upbitServiceRes.body()
            val bithumbMarketCodeList = bithumbServiceRes.body()

            if (upbitMarketCodeList != null && bithumbMarketCodeList != null) {
                exchangeCoinList =
                    (upbitMarketCodeList.map { it.englishName.idMapping() } + bithumbMarketCodeList.map {
                        it.englishName.idMapping()
                    }).toSet().toList()

                val tempList = (upbitMarketCodeList.map {
                    Pair(
                        it.englishName.idMapping(),
                        it.market.split("-")[1]
                    )
                } + bithumbMarketCodeList.map {
                    Pair(it.englishName.idMapping(), it.market.split("-")[1])
                }).toSet().toList()

                val missingCoinList = tempList.filter { (name, symbol) ->
                    cgEngNameCoinMap[name] == null
                }

                exchangeSymbolCoinList.addAll(missingCoinList.map { it.second })
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


    private suspend fun loadNameToIdMap(): Pair<Map<String, String>, Map<String, String>> {
        val file = File(this.filesDir, "cgCoins.json")
        if (!file.exists()) {
            Toast.makeText(this@MainActivity, "코인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return Pair(emptyMap(), emptyMap())
        }

        val jsonString = file.readText()

        val gson = Gson()
        val type = object : TypeToken<List<CgCoinListModel>>() {}.type
        val coinList: List<CgCoinListModel> = gson.fromJson(jsonString, type)

        val nameMap = coinList.associate { it.name to it.id }

        return Pair(
            nameMap,
            coinList.associate { it.symbol to it.id })
    }


    private suspend fun coinCapUpdate() {
        val resultList = mutableListOf<NeedMarketData>()
        val idList = getIdList()

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
            val symbol = coin.symbol.beforeFirebaseMapping()
            val basePath = "/coinInfoData/${symbol.uppercase()}"
            listOf(
                "$basePath/circulatingSupply" to coin.circulatingSupply,
                "$basePath/fullyDilutedValuation" to coin.fullyDilutedValuation,
                "$basePath/image" to coin.image,
                "$basePath/marketCap" to coin.marketCap,
                "$basePath/marketCapRank" to coin.marketCapRank,
                "$basePath/maxSupply" to coin.maxSupply,
                "$basePath/totalSupply" to coin.totalSupply,
                "$basePath/symbol" to symbol,
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
        val idList = getIdList()

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
            resultList.associateBy { coin ->
                val symbol = coin.symbol!!.beforeFirebaseMapping()
                "/coinInfoData/${symbol.uppercase()}/community"
            }

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

    private fun getIdList(): List<String> {
        val nameIdList = exchangeCoinList.mapNotNull { name ->
            cgEngNameCoinMap[name]
        }

        val symbolIdList = exchangeSymbolCoinList.mapNotNull { symbol ->
            cgSymbolCoinMap[symbol.lowercase()]
        }

        return (nameIdList + symbolIdList).toSet().toList()
    }

    private suspend fun getNewCoinIdList(): List<String> {
        // 기존 저장된 symbol 목록 가져오기
        val existingSymbolSet: Set<String> = try {
            val file = File(this.filesDir, "coinInfoData.json")
            if (file.exists()) {
                val jsonString = file.readText()
                val type = object : TypeToken<List<NeedCoinInfoData>>() {}.type
                val existingList: List<NeedCoinInfoData> = gson.fromJson(jsonString, type)

                existingList.mapNotNull { it.symbol?.uppercase() }.toSet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.e("LocalRead", "기존 coinInfoData 읽기 실패", e)
            emptySet()
        }

        // 현재의 ID 리스트 가져오기
        val nameIdList = exchangeCoinList.mapNotNull { name ->
            cgEngNameCoinMap[name]
        }

        val symbolIdList = exchangeSymbolCoinList.mapNotNull { symbol ->
            cgSymbolCoinMap[symbol.lowercase()]
        }

        val allIdList = (nameIdList + symbolIdList).toSet()

        // ID → symbol 매핑
        val idToSymbolMap = allIdList.associateWith { id ->
            try {
                val response = Retrofits.gcInstance.getCoinInfoData(id = id).body()
                response?.symbol?.uppercase()
            } catch (e: Exception) {
                Log.e("SymbolFetch", "symbol 가져오기 실패: $id", e)
                null
            }
        }

        // 기존 symbol에 없는 id만 추출
        val filteredNewIds = idToSymbolMap.filter { (_, symbol) ->
            symbol != null && symbol !in existingSymbolSet
        }.keys.toList()

        Log.d("Filter", "새로 업데이트할 코인 개수: ${filteredNewIds.size}")
        return filteredNewIds
    }

    fun mappingID(symbol: String): String {
        return when (symbol) {
            "USDT" -> {
                "tether"
            }

            "USDC" -> {
                "usd-coin"
            }

            "BAT" -> {
                "basic-attention-token"
            }

            else -> {
                ""
            }
        }
    }

    fun add() {
        cgEngNameCoinMap["Tokamak Network"] = "tokamak-network"
        cgEngNameCoinMap["Basic Attention Token"] = "basic-attention-token"
        cgEngNameCoinMap["USD Coin"] = "usd-coin"
        cgEngNameCoinMap["Tether"] = "tether"
        cgEngNameCoinMap["Pepe"] = "pepe"
        cgEngNameCoinMap["FirmaChain"] = "firmachain"
        cgEngNameCoinMap["Sonic SVM"] = "sonic-svm"
        cgEngNameCoinMap["Polygon Ecosystem Token"] = "matic-network"
        cgEngNameCoinMap["IQ.wiki"] = "everipedia"
        cgEngNameCoinMap["AltLayer"] = "altlayer"
        cgEngNameCoinMap["Polyswarm"] = "polyswarm"
        cgEngNameCoinMap["REI"] = "rei-network"
        cgEngNameCoinMap["Ark"] = "game2"
    }
}