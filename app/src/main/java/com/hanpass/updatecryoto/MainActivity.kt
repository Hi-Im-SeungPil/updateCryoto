package com.hanpass.updatecryoto

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hanpass.updatecryoto.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getMarketList()
    }

    private fun getMarketList() {
        lifecycleScope.launch {
            val upbitServiceRes = Retrofits.upbitInstance.getMarketCodeList()
            val bithumbServiceRes = Retrofits.bithumbInstance.fetchBitThumbMarketCodeList()

            if (upbitServiceRes.isSuccessful && bithumbServiceRes.isSuccessful) {
                val upbitMarketCodeList = upbitServiceRes.body()
                val bithumbMarketCodeList = bithumbServiceRes.body()

                if (upbitMarketCodeList != null && bithumbMarketCodeList != null) {
                    val list =
                        (upbitMarketCodeList.map { it.market.split("-")[1] } + bithumbMarketCodeList.map {
                            it.market.split(
                                "-"
                            )[1]
                        }).toSet().toList()

                    Log.e("list", list.toString())
                }
            }
        }
    }
}