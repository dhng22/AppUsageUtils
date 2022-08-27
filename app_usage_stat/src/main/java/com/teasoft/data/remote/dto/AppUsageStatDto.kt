package com.teasoft.data.remote.dto

import com.teasoft.domain.models.AppUsageStat

data class AppUsageStatDto(val await: Int)

fun AppUsageStatDto.toAppUsageStat():AppUsageStat{
    return AppUsageStat()
}
