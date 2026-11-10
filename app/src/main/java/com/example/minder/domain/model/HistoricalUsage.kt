package com.example.minder.domain.model

//Ragahd-: Represents an application's historical usage for one calendar day.
data class HistoricalUsage(
    val date: Long,
    val totalUsageSeconds: Int
)