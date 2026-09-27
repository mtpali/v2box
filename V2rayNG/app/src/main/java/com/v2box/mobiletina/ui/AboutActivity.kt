package com.v2box.mobiletina.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.v2box.mobiletina.R
import com.v2box.mobiletina.util.SocialVault
import com.v2box.mobiletina.util.Utils
import com.v2box.mobiletina.handler.SettingsManager

/** Store and social page from MobileTinaVPN, with its original links and locations. */
class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<Toolbar>(R.id.a0).apply {
            title = SocialVault.a(if (SettingsManager.getLocale().language == "fa") 18 else 17)
            setNavigationOnClickListener { finish() }
        }
        findViewById<TextView>(R.id.b0).text = SocialVault.a(8)
        findViewById<TextView>(R.id.b1).text = SocialVault.a(4)
        findViewById<TextView>(R.id.b2).text = SocialVault.a(5)
        findViewById<TextView>(R.id.b3).text = SocialVault.a(6)
        findViewById<TextView>(R.id.b4).text = SocialVault.a(7)
        findViewById<TextView>(R.id.b5).text = SocialVault.a(13)
        findViewById<TextView>(R.id.b6).text = SocialVault.a(14)
        findViewById<TextView>(R.id.b7).text = SocialVault.a(15)
        findViewById<TextView>(R.id.b8).text = SocialVault.a(16)

        (0..3).forEach { index ->
            val id = intArrayOf(R.id.a1, R.id.a2, R.id.a3, R.id.a4)[index]
            findViewById<View>(id).setOnClickListener { Utils.openUri(this, SocialVault.a(index)) }
        }
    }
}
