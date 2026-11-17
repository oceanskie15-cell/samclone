package com.samcloner.app.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.samcloner.app.MainActivity
import com.samcloner.app.model.ClonedApp

object ShortcutHelper {

    fun pinCloneShortcut(context: Context, clone: ClonedApp): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val appIconDrawable = runCatching {
            context.packageManager.getApplicationIcon(clone.originalPackageName)
        }.getOrNull()

        val badgedBitmap = createBadgedBitmap(
            drawable = appIconDrawable,
            badgeColor = clone.badgeColorHex.toInt(),
            badgeNumber = clone.userId + 1
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("launch_clone_id", clone.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "clone_${clone.id}")
            .setShortLabel(clone.customLabel)
            .setLongLabel(clone.customLabel)
            .setIcon(IconCompat.createWithBitmap(badgedBitmap))
            .setIntent(launchIntent)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun createBadgedBitmap(
        drawable: Drawable?,
        badgeColor: Int,
        badgeNumber: Int
    ): Bitmap {
        val size = 192
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (drawable != null) {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(drawable.bitmap, size, size, true)
                canvas.drawBitmap(scaled, 0f, 0f, null)
            } else {
                drawable.setBounds(0, 0, size, size)
                drawable.draw(canvas)
            }
        } else {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = badgeColor }
            canvas.drawRoundRect(RectF(8f, 8f, size - 8f, size - 8f), 36f, 36f, bgPaint)
        }

        // Draw clone index badge at bottom-right
        val badgeRadius = 34f
        val cx = size - badgeRadius - 8f
        val cy = size - badgeRadius - 8f
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = badgeColor }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawCircle(cx, cy, badgeRadius, circlePaint)
        canvas.drawCircle(cx, cy, badgeRadius, borderPaint)
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(badgeNumber.toString(), cx, textY, textPaint)

        return bitmap
    }
}
