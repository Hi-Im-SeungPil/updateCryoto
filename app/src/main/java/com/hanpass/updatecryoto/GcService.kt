package com.hanpass.updatecryoto

import com.google.gson.JsonArray
import com.hanpass.updatecryoto.model.CgMarketDataRes
import com.hanpass.updatecryoto.model.CoinInfoData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GcService {
    @GET("api/v3/coins/list")
    suspend fun getCodeList(): Response<JsonArray>

    @GET("api/v3/coins/markets")
    suspend fun getMarketData(
        @Header("x-cg-demo-api-key") key: String = "CG-xbogL8gtLY2pZgQiqQQLHGLd",
        @Query("vs_currency") vsCurrency: String = "krw",
        @Query("ids") ids: String,
        @Query("locale") locale: String = "ko",
        @Query("precision") precision: Int = 3
    ): Response<List<CgMarketDataRes>>

    @GET("api/v3/coins/{id}")
    suspend fun getCoinInfoData(
        @Header("x-cg-demo-api-key") key: String = "CG-xbogL8gtLY2pZgQiqQQLHGLd",
        @Path(value = "id") id: String,
        @Query("localization") locale: Boolean = true,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = false,
        @Query("community_data") communityData: Boolean = true,
        @Query("developer_data") developerData: Boolean = false,
        @Query("sparkline") sparkline: Boolean = false
    ): Response<CoinInfoData>
}