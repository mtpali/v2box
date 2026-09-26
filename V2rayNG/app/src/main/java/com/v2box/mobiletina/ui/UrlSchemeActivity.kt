package com.v2box.mobiletina.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.v2box.mobiletina.AppConfig
import com.v2box.mobiletina.R
import com.v2box.mobiletina.extension.toast
import com.v2box.mobiletina.extension.toastError
import com.v2box.mobiletina.handler.AngConfigManager
import com.v2box.mobiletina.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder

class UrlSchemeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_none)

        try {
            var importing = false
            intent.apply {
                if (action == Intent.ACTION_SEND) {
                    if ("text/plain" == type) {
                        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                            importing = parseUri(it, null)
                        }
                    }
                } else if (action == Intent.ACTION_VIEW) {
                    when (data?.host) {
                        "install-config" -> {
                            val uri: Uri? = intent.data
                            val shareUrl = uri?.getQueryParameter("url").orEmpty()
                            importing = parseUri(shareUrl, uri?.fragment)
                        }

                        "install-sub" -> {
                            val uri: Uri? = intent.data
                            val shareUrl = uri?.getQueryParameter("url").orEmpty()
                            importing = parseUri(shareUrl, uri?.fragment)
                        }

                        else -> {
                            toastError(R.string.toast_failure)
                        }
                    }
                }
            }

            if (!importing) openMainAndFinish()
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Error processing URL scheme", e)
            toastError(R.string.toast_failure)
            openMainAndFinish()
        }
    }

    private fun openMainAndFinish() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun parseUri(uriString: String?, fragment: String?): Boolean {
        if (uriString.isNullOrEmpty()) {
            return false
        }
        LogUtil.i(AppConfig.TAG, uriString)

        var decodedUrl = URLDecoder.decode(uriString, "UTF-8")
        if (Uri.parse(decodedUrl).fragment.isNullOrEmpty() && !fragment.isNullOrEmpty()) {
            decodedUrl += "#${fragment}"
        }
        LogUtil.i(AppConfig.TAG, decodedUrl)
        lifecycleScope.launch {
            try {
                val (count, countSub) = withContext(Dispatchers.IO) {
                    AngConfigManager.importBatchConfig(decodedUrl, "", false)
                }
                toast(if (count + countSub > 0) R.string.import_subscription_success
                    else R.string.import_subscription_failure)
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Failed to import URL scheme", e)
                toastError(R.string.import_subscription_failure)
            }
            openMainAndFinish()
        }
        return true
    }
}
