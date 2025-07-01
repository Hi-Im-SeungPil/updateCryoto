package com.hanpass.updatecryoto

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Retrofits {
    const val UPBIT_BASE_URL = "https://api.upbit.com/"
    const val BITHUMB_BASE_URL = "https://api.bithumb.com/"
    const val GC_BASE_URL = "https://api.upbit.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val upbitInstance: UpbitService by lazy {
        Retrofit.Builder()
            .baseUrl(UPBIT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UpbitService::class.java)
    }

    val bithumbInstance: BithumbService by lazy {
        Retrofit.Builder()
            .baseUrl(BITHUMB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BithumbService::class.java)
    }

    val gcInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GC_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}