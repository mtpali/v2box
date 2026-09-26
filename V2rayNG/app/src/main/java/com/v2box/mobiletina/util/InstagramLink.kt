package com.v2box.mobiletina.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object InstagramLink {
    private const val USERNAME = "mobile.tina2"

    fun open(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("instagram://user?username=$USERNAME")
            ).setPackage("com.instagram.android"))
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.instagram.com/$USERNAME/")))
        }
    }
}
