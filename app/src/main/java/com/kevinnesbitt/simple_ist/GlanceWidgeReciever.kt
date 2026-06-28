package com.kevinnesbitt.simple_ist

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class GlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    // Point the receiver directly to your Glance layout definition
    override val glanceAppWidget: GlanceWidget = GlanceWidget()
}