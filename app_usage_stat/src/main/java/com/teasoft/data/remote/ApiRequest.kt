package com.teasoft.data.remote

import com.teasoft.data.remote.dto.AppUsageStatDto
import com.teasoft.domain.models.AppUsageStat
import retrofit2.Call
import retrofit2.http.*

interface ApiRequest {
    @GET("api/supervise/apps/mostused")
    fun getTopFiveMostUsed(
        @Header("auth-token") authToken: String,
        @Query("id") id: String
    ): Call<List<AppUsageStatDto>>

    @GET("api/supervise/apps")
    fun getAppUsageById(
        @Header("auth-token") authToken: String,
        @Query("id") id: String,
        @Field("packageName") packageName: String,
        @Field("idSupervisee") idSupervisee: String
    ): Call<AppUsageStatDto>

    @PUT("api/supervise/apps/update")
    fun pushData(
        @Header("auth-token") authToken: String,
        @Query("id") id: String,
        @Body usageList: List<List<AppUsageStat>>
    ): Call<String>
}