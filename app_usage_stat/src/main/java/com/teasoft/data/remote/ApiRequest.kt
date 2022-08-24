package com.teasoft.data.remote

import com.teasoft.models.AppUsageStat
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiRequest {
    @GET("...")
    fun getTopFiveMostUsed(): Call<List<AppUsageStat>>

    @GET("...")
    fun getAppUsageById(@Query("id") packageName: String): Call<AppUsageStat>

    @POST("...")
    fun postData(@Body usageList: List<AppUsageStat>):Call<List<AppUsageStat>>
}