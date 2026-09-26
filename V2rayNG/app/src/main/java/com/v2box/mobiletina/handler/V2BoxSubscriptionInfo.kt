package com.v2box.mobiletina.handler

import com.v2box.mobiletina.dto.entities.SubscriptionCache
import com.v2box.mobiletina.dto.entities.SubscriptionItem

/** Metadata advertised in the standard subscription-userinfo response header. */
object V2BoxSubscriptionInfo {
    /** Prefer the selected server's provider, then the open tab, then any enabled provider with data. */
    fun selectForDisplay(subscriptions: List<SubscriptionCache>, preferredIds: List<String>): SubscriptionItem? {
        val available = subscriptions.filter { cache ->
            cache.subscription.enabled && (
                (cache.subscription.trafficTotalBytes ?: 0L) > 0L ||
                    (cache.subscription.expireEpochSeconds ?: 0L) > 0L
            )
        }
        preferredIds.forEach { id ->
            available.firstOrNull { it.guid == id }?.let { return it.subscription }
        }
        return available.firstOrNull()?.subscription
    }

    /** Subtract without overflowing when a provider reports very large byte counters. */
    fun remainingBytes(item: SubscriptionItem): Long? {
        val total = item.trafficTotalBytes?.takeIf { it > 0L } ?: return null
        val afterUpload = total - (item.trafficUploadBytes ?: 0L).coerceIn(0L, total)
        return afterUpload - (item.trafficDownloadBytes ?: 0L).coerceIn(0L, afterUpload)
    }

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
