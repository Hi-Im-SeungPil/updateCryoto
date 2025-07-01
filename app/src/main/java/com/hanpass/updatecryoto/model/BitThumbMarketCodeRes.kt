package com.hanpass.updatecryoto.model

import com.google.gson.annotations.SerializedName

data class BitThumbMarketCodeRes(
    val market: String,

    @SerializedName("english_name")
    val englishName: String,

    @SerializedName("korean_name")
    val koreanName: String,

    @SerializedName("market_warning")
    val marketWarning: String,
)
