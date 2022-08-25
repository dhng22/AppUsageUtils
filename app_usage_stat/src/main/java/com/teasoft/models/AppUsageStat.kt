package com.teasoft.models

import android.app.usage.UsageEvents
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Base64
import com.teasoft.common.Constants
import com.teasoft.utils.AppUsageManager
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * @property packageName the package name associated with this object
 * @property totalUsedTime the time that this application has used
 * @property timeStamp the time stamp of this evaluation
 * @property context the context for initialize package manager
 * @property icon application icon
 * @property name application display name
 */
data class AppUsageStat(
    val context: Context,
    val packageName: String,
    var timeStamp: Date
) {
    var icon: String? = null
    var name: String = ""
    var totalUsedTime: Long = 0
    private var startPoint: Long = 0

    init {
        name = getApplicationName(context)
        icon = getApplicationIcon(context)
    }

    /**
     * Record the start point of an event
     * @param stamp the time stamp of this event
     */
    internal fun onAppStart(stamp: Long) {
        this.startPoint = stamp

    }

    /**
     * Record and calculate the time between start point and end point for total time usage
     * @param stamp the end time stamp of this event
     */
    internal fun onAppEnd(stamp: Long): Unit {
        totalUsedTime += (stamp - startPoint)
    }

    /**
     * For checking if this event has no `UsageEvents.Event.ACTIVITY_RESUMED`, meaning that user were using application throughout the query time
     * @param eventType the event type of this event
     * @param stamp the time stamp of this event
     */
    internal fun firstEventExec(eventType: Int, stamp: Long) {
        if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            onAppStart(stamp)
        } else {
            val helperCalendar = Calendar.getInstance()
            helperCalendar.time = Date(stamp)
            helperCalendar.set(Calendar.HOUR_OF_DAY, 0)
            helperCalendar.set(Calendar.MINUTE, 0)
            onAppStart(helperCalendar.timeInMillis)
            onAppEnd(stamp)
        }
    }

    /**
     *For checking if this event has no `UsageEvents.Event.ACTIVITY_PAUSED`, meaning that user were using application throughout the query time
     * @param eventType the event type of this event
     * @param currentTime the time stamp of current querying time
     */
    internal fun lastEventExec(eventType: Int, currentTime: Long) {
        if (eventType != UsageEvents.Event.ACTIVITY_PAUSED) {
            onAppEnd(currentTime)
        }
    }

    // helper function
    private fun getApplicationIcon(context: Context): String {
        val pm = context.packageManager
        val tempAppIcon = pm.getApplicationIcon(packageName)
        val icon = Bitmap.createBitmap(
            tempAppIcon.intrinsicWidth,
            tempAppIcon.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(icon!!)
        tempAppIcon.setBounds(0, 0, canvas.width, canvas.height)
        tempAppIcon.draw(canvas)

        val byteArrayOutputStream = ByteArrayOutputStream()
        icon.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }
    private fun getApplicationName(context: Context): String {
        val pm = context.packageManager

        return try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown Application"
        }
    }
}
