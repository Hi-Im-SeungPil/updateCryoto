package com.hanpass.updatecryoto

import com.hanpass.updatecryoto.model.BitThumbMarketCodeRes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BithumbService {
    @GET("v1/market/all")
    suspend fun fetchBitThumbMarketCodeList(@Query("isDetails") isDetails: Boolean = true): Response<List<BitThumbMarketCodeRes>>
}