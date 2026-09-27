package com.v2box.mobiletina.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object InstagramLink {
    fun open(context: Context, profileIndex: Int = 1) {
        require(profileIndex in 0..1)
        val webUrl = SocialVault.a(profileIndex)
        val username = Uri.parse(webUrl).lastPathSegment ?: return
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("instagram://user?username=${Uri.encode(username)}")
            ).setPackage("com.instagram.android"))
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse(webUrl)))
        }
    }
}
