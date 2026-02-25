package com.focusapp.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.clockera.R
import java.io.File
import java.io.FileOutputStream

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var previewImage: ImageView
    private lateinit var previewClock: TextView
    private lateinit var fontSpinner: Spinner
    private var selectedImageUri: Uri? = null
    private var selectedFontKey: String = "menil"

    data class FontItem(val name: String, val key: String, val fontResId: Int)


    private val fontList = listOf(
        FontItem("Menil", "menil", R.font.menil_etroit),
        FontItem("Avocado", "avocado", R.font.lt_avocado_regular),
        FontItem("Break", "break", R.font.break_regular),
        FontItem("Dxburst", "dxburst", R.font.dxburst_smooth),
        FontItem("Kiya", "kiya", R.font.kiya_handwrite),
        FontItem("Flaviotte", "flaviotte", R.font.flaviotte),
        FontItem("Awesome", "awesome", R.font.awesome_ways),
        FontItem("Tehegan", "tehegan", R.font.tehegan),
        FontItem("Wonderia", "wonderia", R.font.wonderia),
        FontItem("Kino40", "kino40", R.font.kino40),
        FontItem("1797", "1797", R.font.font_1797_medium_v2),
        FontItem("Glina", "glina", R.font.glina_script_em),
        FontItem("Sentient", "sentient", R.font.sentient_variable),
        FontItem("Chillax", "chillax", R.font.chillax_variable)
    )
    
    
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            previewImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configuration)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        previewImage = findViewById(R.id.preview_image)
        previewClock = findViewById(R.id.preview_clock)
        fontSpinner = findViewById(R.id.font_spinner)

        setupFontSpinner()
        
        updatePreview()

        findViewById<Button>(R.id.btn_pick_image).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<Button>(R.id.btn_save_widget).setOnClickListener {
            val context = this@WidgetConfigurationActivity
            
            // Save image if selected
            selectedImageUri?.let { uri ->
                saveImageToInternalStorage(context, uri, appWidgetId)
            }

            // Save font preference
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("clock_font_$appWidgetId", selectedFontKey).commit()

            // Update the widget with correct provider
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
            
            if (appWidgetInfo != null) {
                val providerClassName = appWidgetInfo.provider.className
                
                if (providerClassName == AestheticWidgetProvider::class.java.name) {
                    updateAestheticAppWidget(context, appWidgetManager, appWidgetId)
                } else {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } else {
                // Fallback: try both update functions to ensure widget gets updated
                try { updateAppWidget(context, appWidgetManager, appWidgetId) } catch (_: Exception) {}
                try { updateAestheticAppWidget(context, appWidgetManager, appWidgetId) } catch (_: Exception) {}
            }

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private fun saveImageToInternalStorage(context: Context, uri: Uri, widgetId: Int) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "widget_bg_$widgetId.png")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            // Save preference indicating we have a custom image
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("has_custom_bg_$widgetId", true).apply()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFontSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            fontList.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fontSpinner.adapter = adapter

        fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedFont = fontList[position]
                selectedFontKey = selectedFont.key
                updatePreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun updatePreview() {
        // Update font
        val selectedFont = fontList.find { it.key == selectedFontKey } ?: fontList[0]
        
        try {
            val typeface = ResourcesCompat.getFont(this@WidgetConfigurationActivity, selectedFont.fontResId)
            previewClock.typeface = typeface
            
            if (selectedImageUri != null) {
                previewImage.setImageURI(selectedImageUri)
                previewImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                previewImage.setImageResource(R.drawable.widget_background)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
