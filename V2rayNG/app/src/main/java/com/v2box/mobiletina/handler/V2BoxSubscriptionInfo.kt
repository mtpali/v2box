package com.v2box.mobiletina.handler

import com.v2box.mobiletina.dto.entities.SubscriptionItem

/** Metadata advertised in the standard subscription-userinfo response header. */
object V2BoxSubscriptionInfo {
    fun apply(item: SubscriptionItem, header: String?) {
        if (header.isNullOrBlank()) return
        val fields = header.split(';').mapNotNull { part ->
            val pair = part.split('=', limit = 2)
            if (pair.size != 2) return@mapNotNull null
            val value = pair[1].trim().toLongOrNull()?.takeIf { it >= 0L }
                ?: return@mapNotNull null
            pair[0].trim().lowercase() to value
        }.toMap()
        item.trafficUploadBytes = fields["upload"] ?: item.trafficUploadBytes
        item.trafficDownloadBytes = fields["download"] ?: item.trafficDownloadBytes
        item.trafficTotalBytes = fields["total"] ?: item.trafficTotalBytes
        item.expireEpochSeconds = fields["expire"] ?: item.expireEpochSeconds
    }
}
