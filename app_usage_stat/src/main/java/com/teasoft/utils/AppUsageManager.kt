package com.teasoft.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.teasoft.models.AppUsageStat
import java.util.*
import kotlin.collections.ArrayList

class AppUsageManager private constructor() {
    companion object {
        internal val defaultLocale = Locale("en", "UK")
//        private val retrofit =
//            retrofit2.Retrofit.Builder().baseUrl(Constants.BASE_API_URL).addConverterFactory(
//                GsonConverterFactory.create()
//            ).build().create(ApiRequest::class.java)

        /**
         *  WhitelistApp for filtering app that's considered exception on some devices with default installation
         *
         *  e.g: Samsung
         */
        private val whiteListApp = arrayListOf(
            // facebook
            "com.facebook.katana",
            "com.google.android.youtube",
            "com.whatsapp",
            // messenger
            "com.facebook.orca",
            "com.instagram.android",
            // wechat
            "com.tencent.mm",
            "com.linkedin.android",
            //tiktok
            "com.zhiliaoapp.musically",
            // douyin
            "com.ss.android.ugc.aweme",
            // weeboo
            "com.sina.weibo"
        )

        /**
         * Query for application time usage
         * @param context the context
         * @return list of `AppUsageStat`
         */
        fun queryAppUsageTime(context: Context): List<AppUsageStat> {
            // init
            val appList = ArrayList<AppUsageStat>()
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager
            val startTime = Calendar.getInstance()
            startTime.set(Calendar.HOUR_OF_DAY, 0)
            startTime.set(Calendar.MINUTE, 0)
            val endTime = Calendar.getInstance()

            //init system app list
            val systemApplication =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { (it.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0 }
                    .map { it.packageName }
                    .filterNot { whiteListApp.contains(it) }

            // query usage list
            val usageList =
                usageStatsManager.queryEvents(startTime.timeInMillis, endTime.timeInMillis)

            // ignore helper function
            fun getAppByPackageName(packageName: String): AppUsageStat {
                for (appUsedTime in appList) {
                    if (appUsedTime.packageName == packageName) {
                        return appUsedTime
                    }
                }
                // not found? create new app
                val newApp = AppUsageStat(
                    context,
                    packageName,
                    Date(startTime.timeInMillis)
                )
                appList.add(newApp)
                return newApp
            }
            // ignore helper function
            fun getNextEvent(events: UsageEvents.Event): Boolean {
                var check= false
                if (usageList.hasNextEvent()) {
                    usageList.getNextEvent(events)
                    @RequiresApi(Build.VERSION_CODES.Q)
                    while (true) {
                        if ((events.eventType == UsageEvents.Event.ACTIVITY_PAUSED || events.eventType == UsageEvents.Event.ACTIVITY_RESUMED)
                            && !systemApplication.contains(events.packageName)
                        ) {
                            check = true
                            break
                        }
                        usageList.getNextEvent(events)
                        continue
                    }
                    return check
                }
                return check
            }


            val event = UsageEvents.Event()
            var appUsageStat:AppUsageStat?=null

            // for the case that user use the application throughout the query request
            if (getNextEvent(event)) {
                appUsageStat = getAppByPackageName(event.packageName)
                appUsageStat.firstEventExec(event.eventType, event.timeStamp)
            }
            // query events
            while (getNextEvent(event)) {
                appUsageStat = getAppByPackageName(event.packageName)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> appUsageStat.onAppStart(event.timeStamp)
                    UsageEvents.Event.ACTIVITY_PAUSED -> appUsageStat.onAppEnd(event.timeStamp)
                }
            }
            // for the case that user use the application throughout the query request
            appUsageStat?.lastEventExec(event.eventType,Calendar.getInstance().timeInMillis)

            return appList
        }


//        /**
//         *  Query for top 5 most used application
//         *  @return list of `AppUsageStat`
//         *
//         * */
//        fun getTopFiveMostUsedApp(): List<AppUsageStat> {
//            val response = retrofit.getTopFiveMostUsed().execute()
//            if (response.message() != "success") {
//                throw HttpException(response)
//            }
//            return response.body()!!
//
//        }
//
//        /**
//         *  Query for a specific application time usage
//         *  @param packageName the id of the `AppUsageStat` to retrieve
//         *  @return a specific `AppUsageStat`
//         */
//        fun getAppUsageStatById(packageName: String): AppUsageStat {
//            val response = retrofit.getAppUsageById(packageName).execute()
//            if (response.message() != "success") {
//                throw HttpException(response)
//            }
//            return response.body()!!
//        }
//
//        /**
//         *  Post data to the server
//         *  @param appUsageList the list that's supposed to be posted
//         */
//        fun postData(appUsageList: List<AppUsageStat>) {
//            val response = retrofit.postData(appUsageList).execute()
//            if (response.message() != "success") {
//                throw HttpException(response)
//            }
//        }
    }
}