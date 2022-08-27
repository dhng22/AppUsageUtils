package com.teasoft.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.teasoft.common.Constants
import com.teasoft.data.remote.ApiRequest
import com.teasoft.data.remote.dto.toAppUsageStat
import com.teasoft.domain.models.AppUsageStat
import com.teasoft.domain.models.AppUsageStatHelper
import com.teasoft.domain.models.toAppUsageStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class AppUsageManager private constructor() {
    companion object {
        private val defaultLocale = Locale("en", "UK")
        private val retrofit =
            retrofit2.Retrofit.Builder().baseUrl(Constants.BASE_API_URL).addConverterFactory(
                GsonConverterFactory.create()
            ).build().create(ApiRequest::class.java)

        /**
         * Query for application time usage, use this for daily query
         * @param context the context
         * @param timeStamp the Calendar that specify a day to query, for present query, pass `Calendar.getInstance()` in
         * @return list of `AppUsageStat`
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        fun queryDayAppUsageTime(context: Context, timeStamp: Calendar): List<AppUsageStat> {
            // init
            val appList = ArrayList<AppUsageStatHelper>()
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val startTime = timeStamp.clone(0)
            startTime.set(Calendar.HOUR_OF_DAY, 0)
            startTime.set(Calendar.MINUTE, 0)
            startTime.set(Calendar.SECOND, 0)
            val endTime = timeStamp.clone(0)

            // query application usage list
            val usageList =
                usageStatsManager.queryEvents(startTime.timeInMillis, endTime.timeInMillis)

            // ignore helper function
            fun getAppByPackageName(packageName: String): AppUsageStatHelper {
                for (appUsedTime in appList) {
                    if (appUsedTime.packageName == packageName) {
                        return appUsedTime
                    }
                }
                // not found? create new app
                val newApp = AppUsageStatHelper(
                    context,
                    packageName,
                    Date(startTime.timeInMillis)
                )
                appList.add(newApp)
                return newApp
            }
            // ignore helper function
            fun getNextEvent(events: UsageEvents.Event): Boolean {
                var check = false
                if (usageList.hasNextEvent()) {
                    usageList.getNextEvent(events)
                    @RequiresApi(Build.VERSION_CODES.Q)
                    while (true) {
                        if ((events.eventType == UsageEvents.Event.ACTIVITY_PAUSED || events.eventType == UsageEvents.Event.ACTIVITY_RESUMED)
                        ) {
                            check = true
                            break
                        }
                        if (!usageList.hasNextEvent()) {
                            check = false
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
            var appUsageStat: AppUsageStatHelper? = null

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
            appUsageStat?.lastEventExec(event.eventType, Calendar.getInstance().timeInMillis)

            return appList.map { it.toAppUsageStat() }
        }


        /**
         * Query for week time usage, use this for the first time query user data to the server
         *
         * `Note`: Since android only keep the query value for 10 days include present, the maximum amount of week you can query in practical is 0 or 1 (0 means that only this week)
         * 
         * @param context the context
         * @param weekRollBackAmount the amount of weeks to query
         * @return a flow of each day app's usage information
         *
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        fun queryWeekUsageTime(
            context: Context,
            weekRollBackAmount: Int
        ): Flow<List<AppUsageStat>> = flow {
            val calendar = Calendar.getInstance(defaultLocale)
            var dayToQuery =
                (if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {
                    6
                } else {
                    (calendar.get(Calendar.DAY_OF_WEEK) - 2)
                }) + (7 * weekRollBackAmount)

            while (dayToQuery > 0) {
                calendar.roll(Calendar.DATE, false)
                dayToQuery--
                val dailyList = ArrayList(queryDayAppUsageTime(context, calendar))
                if (dailyList.size > 0) {
                    emit(dailyList)
                }
            }
        }
        /**
         *  Query for top 5 most used application
         *  @param authToken real-time auth token return for each supervisor login session
         *  @param id the id of supervisee
         *  @return list of `AppUsageStat`
         *
         * */
        fun getTopFiveMostUsedApp(authToken: String,id: String): List<AppUsageStat> {
            val response = retrofit.getTopFiveMostUsed(authToken, id).execute()
            if (response.message() != "success") {
                throw HttpException(response)
            }
            return response.body()!!.map { it.toAppUsageStat() }

        }

        /**
         *  Query for a specific application time usage
         *  @param authToken real-time auth token return for each supervisor login session
         *  @param id the id of supervisee
         *  @param packageName the package name of the application
         *  @param superviseeId id of the supervisee that contain this application statistic
         *  @return a specific `AppUsageStat`
         */
        fun getAppUsageStatById(authToken: String,id: String,packageName:String,superviseeId:String): AppUsageStat {
            val response = retrofit.getAppUsageById(authToken,id,packageName,superviseeId).execute()
            if (response.message() != "success") {
                throw HttpException(response)
            }
            return response.body()!!.toAppUsageStat()
        }

        /**
         *  Post data to the server
         *  @param authToken real-time auth token return for each supervisor login session
         *  @param id the id of supervisee
         *  @param appUsageList the list that's supposed to be posted
         */
        fun postData(authToken: String, id: String, appUsageList: List<List<AppUsageStat>>) {
            val response = retrofit.pushData(authToken, id, appUsageList).execute()
            if (response.message() != "success") {
                throw HttpException(response)
            }
        }
    }
}

/**
 * Helper method, purpose to clone a calendar
 * @param ignoreParams this parameter has no effect, can pass in any integer value
 * @return a copy of this calendar with different reference
 */
@Suppress("UNUSED_PARAMETER")
private fun Calendar.clone(ignoreParams: Int): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = Date(this.timeInMillis)
    return calendar
}