package com.teasoft.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.teasoft.common.Constants
import com.teasoft.data.remote.ApiRequest
import com.teasoft.models.AppUsageStat
import retrofit2.HttpException
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AppUsageManager private constructor() {
    companion object {
        private val retrofit =
            retrofit2.Retrofit.Builder().baseUrl(Constants.BASE_API_URL).addConverterFactory(
                GsonConverterFactory.create()
            ).build().create(ApiRequest::class.java)
        private val whiteListApp = arrayListOf<String>(
            "com.facebook.katana",
            "com.google.android.youtube",
            "com.whatsapp",
            "com.facebook.orca",
            "com.instagram.android",
            "com.tencent.mm",
            "com.linkedin.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.aweme",
            "com.sina.weibo"
        )

        /**
         * Query for application time usage
         * @param isDaily true for daily time query, false for weekly
         * @return list of application time usage
         */
        fun queryAllAppUsageTime(context: Context, isDaily: Boolean): List<AppUsageStat> {
            // init
            val appList = ArrayList<AppUsageStat>()
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager
            val startTime = Calendar.getInstance()
            val endTime = Calendar.getInstance()
            //init system app list
            val systemApplication =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { (it.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0 }
                    .map { it.packageName }
                    .filterNot { whiteListApp.contains(it) }

            // ignore helper function
            fun getAppByPackageName(packageName: String): AppUsageStat {
                for (appUsedTime in appList) {
                    if (appUsedTime.packageName == packageName) {
                        return appUsedTime
                    }
                }
                val newApp = AppUsageStat()
                newApp.packageName = packageName
                newApp.isDaily = isDaily
                newApp.timeStampStart =
                    SimpleDateFormat(Constants.TIME_FORMAT).format(Date(startTime.timeInMillis))
                appList.add(newApp)
                return newApp
            }
            // setting up calendar
            setUpCalendar(startTime, endTime, isDaily)


            // query usage list
            val usageList =
                usageStatsManager.queryEvents(startTime.timeInMillis, endTime.timeInMillis)
            while (usageList.hasNextEvent()) {
                val event = UsageEvents.Event()
                usageList.getNextEvent(event)
                // ignore system application
                if (systemApplication.contains(event.packageName)) {
                    continue
                }
                val appUsedTime = getAppByPackageName(event.packageName)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> appUsedTime.startUsingPoint(event.timeStamp)
                    UsageEvents.Event.ACTIVITY_PAUSED -> appUsedTime.endUsingPoint(event.timeStamp)
                }
            }
            return appList
        }


        /**
         *  Query for top 5 most used application
         *  @return list of application with associated information
         *
         * */
        fun getTopFiveMostUsedApp(): List<AppUsageStat> {
            val response = retrofit.getTopFiveMostUsed().execute()
            if (response.message() != "success") {
                throw HttpException(response)
            }
            return response.body()!!

        }

        /**
         *  Query for a specific application time usage
         *  @return a specific application with time usage
         */
        fun getAppUsageStatById(packageName: String): AppUsageStat {
            val response = retrofit.getAppUsageById(packageName).execute()
            if (response.message() != "success") {
                throw HttpException(response)
            }
            return response.body()!!
        }

        /**
         *  Post data to the server
         *  @param appUsageList the list that's supposed to be posted
         */
        fun postData(appUsageList: List<AppUsageStat>) {
            val response = retrofit.postData(appUsageList).execute()
            if (response.message() != "success") {
                throw HttpException(response)
            }
        }

        // helper function
        private fun setUpCalendar(startTime: Calendar, endTime: Calendar, isDaily: Boolean): Unit {
            when (isDaily) {
                true -> {
                    startTime.set(Calendar.HOUR_OF_DAY, 0)
                    startTime.set(Calendar.MINUTE, 0)
                }
                false -> {
                    startTime.set(Calendar.HOUR_OF_DAY, 0)
                    startTime.set(Calendar.MINUTE, 0)
                    startTime.set(
                        Calendar.DAY_OF_WEEK,
                        Calendar.getInstance(Locale("en", "UK")).firstDayOfWeek
                    )
                }
            }

        }
    }
}