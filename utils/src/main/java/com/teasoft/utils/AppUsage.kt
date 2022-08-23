package com.teasoft.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.teasoft.common.Constants
import com.teasoft.models.AppUsedTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AppUsage private constructor() {
    companion object {
        /**
         * Query for application time usage
         * @param isDaily true for daily time query, false for weekly
         * @return list of application time usage
         */
        fun queryUsageTime(context: Context, isDaily: Boolean): List<AppUsedTime> {
            // init app list and stat manager
            val appList = ArrayList<AppUsedTime>()
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val startTime = Calendar.getInstance()
            val endTime = Calendar.getInstance()

            // ignore helper function
            fun getAppByPackageName(packageName: String): AppUsedTime {
                for (appUsedTime in appList) {
                    if (appUsedTime.packageName == packageName) {
                        return appUsedTime
                    }
                }
                val newApp = AppUsedTime()
                newApp.packageName = packageName
                newApp.isDaily = isDaily
                newApp.timeStampStart = SimpleDateFormat(Constants.TIME_FORMAT).format(Date(startTime.timeInMillis))
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

                val appUsedTime = getAppByPackageName(event.packageName)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> appUsedTime.startUsingPoint(event.timeStamp)
                    UsageEvents.Event.ACTIVITY_PAUSED -> appUsedTime.endUsingPoint(event.timeStamp)
                }
            }
            return appList
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
                        Calendar.getInstance(Locale("en","UK")).firstDayOfWeek
                    )
                }
            }

        }
    }
}