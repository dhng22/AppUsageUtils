package com.teasoft.models

/**
 * @property packageName the package name associated with this object
 * @property totalUsedTime the time that this application has used
 * @property timeStampStart the time stamp of this evaluation
 * @property isDaily is this evaluation daily or weekly. true for daily and otherwise
 */
class AppUsageStat {
    private var startPoint: Long = 0
    var packageName: String? = null
    var totalUsedTime: Long = 0
    var timeStampStart: String? = null
    var isDaily: Boolean = true

    internal fun startUsingPoint(stamp: Long): Unit {
        this.startPoint = stamp
    }

    internal fun endUsingPoint(stamp: Long): Unit {
        totalUsedTime += (stamp - startPoint)
    }
}
