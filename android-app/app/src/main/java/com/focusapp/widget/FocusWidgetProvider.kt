package com.focusapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.focusapp.R
import com.focusapp.ui.MainActivity

class FocusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Get font preference
    val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    val specificFont = widgetPrefs.getString("clock_font_$appWidgetId", null)
    
    val clockFont = if (specificFont != null) {
        specificFont
    } else {
        // Fallback to global preference
        val prefs = context.getSharedPreferences("focus_app_settings", Context.MODE_PRIVATE)
        prefs.getString("clock_font", "menil") ?: "menil"
    }

    // Select layout based on font
    val layoutId = when (clockFont) {
        "avocado" -> R.layout.widget_clock_avocado
        "break" -> R.layout.widget_clock_break
        "dxburst" -> R.layout.widget_clock_dxburst
        "kiya" -> R.layout.widget_clock_kiya
        "flaviotte" -> R.layout.widget_clock_flaviotte
        "awesome" -> R.layout.widget_clock_awesome
        "tehegan" -> R.layout.widget_clock_tehegan
        "wonderia" -> R.layout.widget_clock_wonderia
        "kino40" -> R.layout.widget_clock_kino40
        "1797" -> R.layout.widget_clock_1797
        "glina" -> R.layout.widget_clock_glina
        "sentient" -> R.layout.widget_clock_sentient
        "chillax" -> R.layout.widget_clock_chillax
        else -> R.layout.widget_clock_menil // Default to menil
    }

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, layoutId)



    // Try to load custom background
    if (widgetPrefs.getBoolean("has_custom_bg_$appWidgetId", false)) {
        val file = java.io.File(context.filesDir, "widget_bg_$appWidgetId.png")
        if (file.exists()) {
             try {
                // Decode with sampling to avoid TransactionTooLargeException
                val options = android.graphics.BitmapFactory.Options()
                options.inJustDecodeBounds = true
                android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
                
                // Calculate inSampleSize
                // Target around 500x500 for a 2x2 widget (plenty of resolution)
                options.inSampleSize = calculateInSampleSize(options, 500, 500)
                
                options.inJustDecodeBounds = false
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
                
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_background, bitmap)
                }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }
    
    // Create an Intent to launch MainActivity
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Get the layout for the App Widget and attach an on-click listener
    views.setOnClickPendingIntent(R.id.widget_timer, pendingIntent)
    // widget_title might not exist in all layouts if I removed it, but I should probably check.
    // In the new layouts I removed widget_title and only have widget_timer (TextClock).
    // So I will remove the listener for widget_title to avoid crashes if ID is missing? 
    // Actually setOnClickPendingIntent doesn't crash if viewId is missing, it just does nothing.
    // But I'll remove it since my new layouts don't have it.

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

// Helper to calculate sample size
internal fun calculateInSampleSize(
    options: android.graphics.BitmapFactory.Options, 
    reqWidth: Int, 
    reqHeight: Int
): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}
