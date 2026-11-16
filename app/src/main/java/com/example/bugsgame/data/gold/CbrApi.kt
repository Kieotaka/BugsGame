package com.example.bugsgame.data.gold

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface CbrApi {
    @Streaming
    @GET("scripts/xml_metall.asp")
    suspend fun getMetals(
        @Query("date_req1") dateFrom: String,
        @Query("date_req2") dateTo: String
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://www.cbr.ru/"
    }
}