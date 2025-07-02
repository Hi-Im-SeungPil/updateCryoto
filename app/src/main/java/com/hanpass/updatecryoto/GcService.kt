package com.hanpass.updatecryoto

import com.hanpass.updatecryoto.model.CgMarketDataRes
import com.hanpass.updatecryoto.model.CoinInfoData
import org.json.JSONArray
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GcService {
    @GET("api/v3/coins/list")
    suspend fun getCodeList(): Response<JSONArray>

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
        @Header("x-cg-demo-api-key") key: String = "CG-xbogL8gtLY2pZgQiqQQLHGLd"
    ): Response<CoinInfoData>
}