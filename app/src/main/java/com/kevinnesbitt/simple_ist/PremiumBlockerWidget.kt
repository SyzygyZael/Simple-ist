import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider

class PremiumBlockerWidget : GlanceAppWidget() {

    // 🔄 Override provideGlance instead of provideContent
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // Then call the library function provideContent inside it
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(day = Color.DarkGray, night = Color.DarkGray))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔒 Premium Feature",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(day = Color.White, night = Color.White)
                        )
                    )
                    Text(
                        text = "Upgrade to place multiple widgets on your home screen.",
                        style = TextStyle(
                            color = ColorProvider(day = Color.LightGray, night = Color.LightGray)
                        ),
                        modifier = GlanceModifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}