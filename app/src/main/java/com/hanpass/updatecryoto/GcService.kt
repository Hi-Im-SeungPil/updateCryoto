package com.hanpass.updatecryoto

import com.hanpass.updatecryoto.model.UpbitMarketCodeRes
import org.json.JSONArray
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface GcService {
    @GET("api/v3/coins/list")
    suspend fun getCodeList(): Response<JSONArray>

    @GET("api/v3/coins/markets")
    suspend fun getMarketData(
        @Header("x-cg-demo-api-key") key: String = "CG-xbogL8gtLY2pZgQiqQQLHGLd"
    ): Response<>

    @GET("api/v3/coins/{id}")
    suspend fun getCoinInfoData(
        @Header("x-cg-demo-api-key") key: String = "CG-xbogL8gtLY2pZgQiqQQLHGLd"
    ): Response<>
}