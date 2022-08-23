package com.teasoft.models


class AppUsedTime {
    var packageName: String? = null
    private var startPoint: Long = 0
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
