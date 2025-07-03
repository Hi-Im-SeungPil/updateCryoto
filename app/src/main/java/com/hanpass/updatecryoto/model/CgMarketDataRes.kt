package com.hanpass.updatecryoto.model

import com.google.gson.annotations.SerializedName

data class CgMarketDataRes(
    @SerializedName("ath")
    val ath: Double,

    @SerializedName("ath_change_percentage")
    val athChangePercentage: Double,

    @SerializedName("ath_date")
    val athDate: String,

    @SerializedName("atl")
    val atl: Double,

    @SerializedName("atl_change_percentage")
    val atlChangePercentage: Double,

    @SerializedName("atl_date")
    val atlDate: String,

    @SerializedName("circulating_supply")
    val circulatingSupply: Double, // 필요

    @SerializedName("current_price")
    val currentPrice: Double,

    @SerializedName("fully_diluted_valuation")
    val fullyDilutedValuation: Double, // 필요

    @SerializedName("high_24h")
    val high24h: Double,

    @SerializedName("id")
    val id: String,

    @SerializedName("image")
    val image: String, // 필요

    @SerializedName("last_updated")
    val lastUpdated: String,

    @SerializedName("low_24h")
    val low24h: Double,

    @SerializedName("market_cap")
    val marketCap: Double, // 필요

    @SerializedName("market_cap_change_24h")
    val marketCapChange24h: Double,

    @SerializedName("market_cap_change_percentage_24h")
    val marketCapChangePercentage24h: Double,

    @SerializedName("market_cap_rank")
    val marketCapRank: Int,

    @SerializedName("max_supply")
    val maxSupply: Double,

    @SerializedName("name")
    val name: String,

    @SerializedName("price_change_24h")
    val priceChange24h: Double,

    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("total_supply")
    val totalSupply: Double,

    @SerializedName("total_volume")
    val totalVolume: Double
) {
    fun parseNeedData(): NeedMarketData {
        return NeedMarketData(
            circulatingSupply = circulatingSupply,
            fullyDilutedValuation = fullyDilutedValuation,
            image = image,
            marketCap = marketCap,
            marketCapRank = marketCapRank,
            maxSupply = maxSupply,
            totalSupply = totalSupply,
            symbol = symbol,
        )
    }
}

data class NeedMarketData(
    val circulatingSupply: Double = 0.0,
    val fullyDilutedValuation: Double = 0.0,
    val image: String = "",
    val marketCap: Double = 0.0,
    val marketCapRank: Int = 0,
    val maxSupply: Double = 0.0,
    val totalSupply: Double = 0.0,
    val symbol: String = "",
)