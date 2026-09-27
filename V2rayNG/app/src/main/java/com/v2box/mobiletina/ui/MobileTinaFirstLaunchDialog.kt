package com.v2box.mobiletina.ui

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.v2box.mobiletina.R
import com.v2box.mobiletina.util.InstagramLink
import com.v2box.mobiletina.util.SocialVault
import com.v2box.mobiletina.util.Utils
import java.security.MessageDigest

/** First-install social links; the labels and destinations are decrypted only for display. */
internal object MobileTinaFirstLaunchDialog {
    private val expected = byteArrayOf(
        103, 97, 88, -112, 124, -27, 49, 48, -92, 41, -79, -120, 72, 20, -71, 78,
        -14, 85, 65, -39, -5, -108, 90, 93, 21, -21, 46, -4, -125, 92, 94, 63
    )

    fun showOnce(activity: AppCompatActivity, onDismiss: () -> Unit): Dialog? {
        val preferences = activity.getSharedPreferences("v2box_first_launch", Context.MODE_PRIVATE)
        if (preferences.getBoolean("social_notice_v1", false) || activity.isFinishing || activity.isDestroyed) {
            return null
        }

        val labels = arrayOf(SocialVault.a(9), SocialVault.a(10), SocialVault.a(12))
        val destinations = arrayOf(SocialVault.a(0), SocialVault.a(1), SocialVault.a(3))
        verify(labels, destinations)

        val blue = ContextCompat.getColor(activity, R.color.v2box_dialog_stroke)
        val textColor = ContextCompat.getColor(activity, R.color.v2box_text)
        val surfaceColor = ContextCompat.getColor(activity, R.color.v2box_card)
        val controlColor = ContextCompat.getColor(activity, R.color.v2box_control)

        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            setPadding(dp(activity, 22), dp(activity, 22), dp(activity, 22), dp(activity, 20))
            this.background = GradientDrawable().apply {
                cornerRadius = dp(activity, 24).toFloat()
                setColor(surfaceColor)
                setStroke(dp(activity, 2), blue)
            }
        }

        root.addView(View(activity).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(blue, Color.rgb(102, 209, 239), blue)
            ).apply { cornerRadius = dp(activity, 4).toFloat() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 5)).apply {
            bottomMargin = dp(activity, 19)
        })

        root.addView(TextView(activity).apply {
            text = activity.getString(R.string.title_about)
            setTextColor(textColor)
            textSize = 20f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(activity, 18)
        })

        labels.forEachIndexed { index, label ->
            root.addView(MaterialButton(activity).apply {
                text = label
                isAllCaps = false
                textSize = 14f
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                textDirection = View.TEXT_DIRECTION_LTR
                setTextColor(textColor)
                backgroundTintList = ColorStateList.valueOf(controlColor)
                strokeColor = ColorStateList.valueOf(blue)
                strokeWidth = dp(activity, 1)
                cornerRadius = dp(activity, 14)
                icon = ContextCompat.getDrawable(activity,
                    if (index < 2) R.drawable.ic_instagram_24dp else R.drawable.ic_about_24dp)
                iconTint = ColorStateList.valueOf(blue)
                iconSize = dp(activity, 20)
                iconPadding = dp(activity, 12)
                setOnClickListener {
                    if (index < 2) InstagramLink.open(activity, index)
                    else Utils.openUri(activity, destinations[index])
                }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 54)).apply {
                bottomMargin = dp(activity, 10)
            })
        }

        val dialog = Dialog(activity)
        root.addView(MaterialButton(activity).apply {
            setText(R.string.v2box_dialog_close)
            isAllCaps = false
            textSize = 14f
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(blue)
            cornerRadius = dp(activity, 14)
            setOnClickListener { dialog.dismiss() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 50)).apply {
            topMargin = dp(activity, 6)
        })

        dialog.setContentView(root)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnDismissListener { onDismiss() }
        return try {
            dialog.show()
            dialog.window?.let { window ->
                window.setBackgroundDrawableResource(android.R.color.transparent)
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                window.setDimAmount(0.58f)
                val width = (activity.resources.displayMetrics.widthPixels * 0.88f).toInt()
                    .coerceAtMost(dp(activity, 430))
                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            }
            preferences.edit().putBoolean("social_notice_v1", true).apply()
            dialog
        } catch (_: RuntimeException) {
            dialog.setOnDismissListener(null)
            if (dialog.isShowing) dialog.dismiss()
            null
        }
    }

    private fun verify(labels: Array<String>, destinations: Array<String>) {
        val digest = MessageDigest.getInstance("SHA-256")
        (labels.asList() + destinations.asList()).forEach { value ->
            digest.update(value.toByteArray(Charsets.UTF_8))
            digest.update(0.toByte())
        }
        if (!MessageDigest.isEqual(digest.digest(), expected)) throw SecurityException("Invalid social links")
    }

    private fun dp(activity: AppCompatActivity, value: Int): Int =
        (value * activity.resources.displayMetrics.density + 0.5f).toInt()
}
