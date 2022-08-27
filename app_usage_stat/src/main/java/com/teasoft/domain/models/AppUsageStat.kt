package com.teasoft.domain.models

import java.util.*

data class AppUsageStat(
    val packageName: String,
    var timeStamp: Date,
    var icon: String,
    var name: String,
    var totalUsedTime: Long
)
