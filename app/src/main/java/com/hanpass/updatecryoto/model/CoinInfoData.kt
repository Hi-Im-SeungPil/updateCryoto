package com.hanpass.updatecryoto.model

import com.google.gson.annotations.SerializedName

data class CoinInfoData(
    val links: Links,
    @SerializedName("genesis_date")
    val genesisDate: String
)

data class Links(
    val homePage: List<String>,
    val whitepaper: String,
    @SerializedName("twitter_screen_name")
    val twitterScreenName: String,

    @SerializedName("blockchain_site")
    val blockchainSite: String
)
