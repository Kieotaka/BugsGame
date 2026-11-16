package com.example.bugsgame.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.bugsgame.MainActivity
import com.example.bugsgame.R
import com.example.bugsgame.data.gold.GoldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class GoldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_gold_ball)

        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.ballContainer, pending)

        val repo = GoldRepository.get()
        repo.cachedPrice?.let { views.setTextViewText(R.id.tvGoldPrice, format(it)) }
        manager.updateAppWidget(widgetId, views)

        CoroutineScope(Dispatchers.IO).launch {
            val price = repo.fetchGoldPrice()
            if (price != null) {
                val updated = RemoteViews(context.packageName, R.layout.widget_gold_ball)
                updated.setTextViewText(R.id.tvGoldPrice, format(price))
                updated.setOnClickPendingIntent(R.id.ballContainer, pending)
                manager.updateAppWidget(widgetId, updated)
            }
        }
    }

    private fun format(v: Double): String =
        String.format(Locale.US, "%.2f", v)
}