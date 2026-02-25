package com.focusapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import android.widget.RemoteViews
import com.clockera.R
import com.focusapp.ui.MainActivity

class AestheticWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAestheticAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAestheticAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_aesthetic)

    // 1. Handle Font Selection
    val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    val specificFont = widgetPrefs.getString("clock_font_$appWidgetId", null)
    
    val clockFont = specificFont ?: "menil"

    // Hide all clocks first
    val clockIds = listOf(
        R.id.clock_menil, R.id.clock_avocado, R.id.clock_break, R.id.clock_dxburst,
        R.id.clock_kiya, R.id.clock_flaviotte, R.id.clock_awesome, R.id.clock_tehegan,
        R.id.clock_wonderia, R.id.clock_kino40, R.id.clock_1797, R.id.clock_glina,
        R.id.clock_sentient, R.id.clock_chillax
    )
    
    for (id in clockIds) {
        views.setViewVisibility(id, View.GONE)
    }

    // Show selected clock
    val selectedId = when (clockFont) {
        "avocado" -> R.id.clock_avocado
        "break" -> R.id.clock_break
        "dxburst" -> R.id.clock_dxburst
        "kiya" -> R.id.clock_kiya
        "flaviotte" -> R.id.clock_flaviotte
        "awesome" -> R.id.clock_awesome
        "tehegan" -> R.id.clock_tehegan
        "wonderia" -> R.id.clock_wonderia
        "kino40" -> R.id.clock_kino40
        "1797" -> R.id.clock_1797
        "glina" -> R.id.clock_glina
        "sentient" -> R.id.clock_sentient
        "chillax" -> R.id.clock_chillax
        else -> R.id.clock_menil
    }
    views.setViewVisibility(selectedId, View.VISIBLE)


    // 2. Handle Image Loading with Rounded Corners
    if (widgetPrefs.getBoolean("has_custom_bg_$appWidgetId", false)) {
        val file = java.io.File(context.filesDir, "widget_bg_$appWidgetId.png")
        if (file.exists()) {
             try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(file.absolutePath, options)
                
                options.inSampleSize = calculateInSampleSize(options, 500, 500)
                
                options.inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                
                if (bitmap != null) {
                    // Apply rounded corners manually since typical ImageView attributes don't work on RemoteViews
                    val roundedBitmap = getRoundedCornerBitmap(bitmap, 60f) // approx 20dp in pixels (density dependent ideally, but fixed for now)
                    views.setImageViewBitmap(R.id.widget_image, roundedBitmap)
                }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    } else {
         // Default image if needed, or leave as is defined in XML
    }

    // 3. Pending Intent
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.clock_container, pendingIntent)
    views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

// Helper function to create rounded bitmap
fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Float): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)
    val roundPx = pixels

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    return output
}
