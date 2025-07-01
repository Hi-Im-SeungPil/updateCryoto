package com.hanpass.updatecryoto.model

import com.google.gson.annotations.SerializedName


data class UpbitMarketCodeRes(
    @SerializedName("english_name")
    val englishName: String,
    @SerializedName("korean_name")
    val koreanName: String,
    val market: String,
)
