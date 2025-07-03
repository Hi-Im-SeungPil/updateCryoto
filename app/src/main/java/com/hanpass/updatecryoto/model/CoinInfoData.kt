package com.hanpass.updatecryoto.model

import com.google.gson.annotations.SerializedName

data class CoinInfoData(
    val symbol: String,
    val links: Links,

    @SerializedName("genesis_date")
    val genesisDate: String? = "",

    val description: Description
) {
    fun parseNeedData(): NeedCoinInfoData {
        return NeedCoinInfoData(
            homePage = links.homepage.firstOrNull() ?: "",
            whitePaper = links.whitepaper,
            twitterScreenName = links.twitterScreenName,
            blockchainSite = links.blockchainSite.firstOrNull() ?: "",
            description = description.ko,
            genesisDate = genesisDate,
            symbol = symbol
        )
    }
}

data class NeedCoinInfoData(
    val homePage: String? = "",
    val whitePaper: String? = "",
    val twitterScreenName: String? = "",
    val blockchainSite: String? = "",
    val description: String? = "",
    val genesisDate: String? = "",
    val symbol: String? = "",
)

data class Links(
    val homepage: List<String>,
    val whitepaper: String? = "",
    @SerializedName("twitter_screen_name")
    val twitterScreenName: String? = "",

    @SerializedName("blockchain_site")
    val blockchainSite: List<String>
)

data class Description(
    val ko: String? = ""
)