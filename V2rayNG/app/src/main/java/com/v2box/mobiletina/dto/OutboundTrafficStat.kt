package com.v2box.mobiletina.dto

data class OutboundTrafficStat(
    val tag: String,
    val direction: String,
    val value: Long,
)