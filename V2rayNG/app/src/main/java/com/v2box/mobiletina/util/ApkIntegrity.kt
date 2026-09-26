package com.v2box.mobiletina.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.v2box.mobiletina.BuildConfig
import java.security.MessageDigest

/** Stops a release APK that has been modified and signed with a different certificate. */
internal object ApkIntegrity {
    fun verify(context: Context) {
        val expected = BuildConfig.TRUSTED_SIGNER_SHA256
        if (BuildConfig.DEBUG && expected.isEmpty()) return
        if (expected.length != 64 || context.packageName != BuildConfig.APPLICATION_ID) {
            throw SecurityException("Untrusted APK")
        }

        @Suppress("DEPRECATION")
        val signers: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_SIGNATURES
            ).signatures ?: emptyArray()
        }

        val actual = signers.singleOrNull()?.toByteArray()?.let { certificate ->
            MessageDigest.getInstance("SHA-256").digest(certificate)
        } ?: throw SecurityException("Untrusted APK")
        val trusted = expected.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        if (!MessageDigest.isEqual(actual, trusted)) {
            throw SecurityException("Untrusted APK")
        }
    }
}
