package com.v2box.mobiletina.ui

import android.content.Intent
import android.os.Bundle
import com.v2box.mobiletina.R
import com.v2box.mobiletina.extension.toastError
import com.v2box.mobiletina.extension.toastSuccess
import com.v2box.mobiletina.handler.AngConfigManager

class ScScannerActivity : HelperBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_none)
        importQRcode()
    }

    private fun importQRcode() {
        launchQRCodeScanner { scanResult ->
            if (scanResult != null) {
                val (count, countSub) = AngConfigManager.importBatchConfig(scanResult, "", false)

                if (count + countSub > 0) {
                    toastSuccess(R.string.toast_success)
                } else {
                    toastError(R.string.toast_failure)
                }

                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
    }
}