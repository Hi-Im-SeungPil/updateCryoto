package com.hanpass.updatecryoto

import com.hanpass.updatecryoto.model.UpbitMarketCodeRes
import retrofit2.Response
import retrofit2.http.GET

interface UpbitService {
    @GET("v1/market/all?isDetails=true")
    suspend fun getMarketCodeList(): Response<List<UpbitMarketCodeRes>>
}