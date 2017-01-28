package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.example.buhalo.lazyir.IrMethods;
import com.example.buhalo.lazyir.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link IrWidgConfigureActivity IrWidgConfigureActivity}
 */
public class IrWidg extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = IrWidgConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ir_widg);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget

        Intent active = new Intent(context, IrWidg.class);
        active.setAction("OnlyTvButton");
        Intent active2 = new Intent(context, IrWidg.class);
        active2.setAction("OnlyAudioButton");
        Intent active3 = new Intent(context, IrWidg.class);
        active3.setAction("TvAndAudioButton");

        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
        PendingIntent actionPendingIntent2 = PendingIntent.getBroadcast(context, 0, active2, 0);
        PendingIntent actionPendingIntent3 = PendingIntent.getBroadcast(context, 0, active3, 0);
        views.setOnClickPendingIntent(R.id.button4,actionPendingIntent2);
        views.setOnClickPendingIntent(R.id.button5,actionPendingIntent3);
        views.setOnClickPendingIntent(R.id.button6,actionPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        System.out.println("dada");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            IrWidgConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }


    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if ("TvAndAudioButton".equals(action)) {
            IrMethods.processOnlyAudio(null,context);
            IrMethods.processOnlyTv(null,context);
        }
        if ("OnlyAudioButton".equals(action)) {
            IrMethods.processOnlyAudio(null,context);
        }
        if ("OnlyTvButton".equals(action)) {
            IrMethods.processOnlyTv(null,context);
        }
        super.onReceive(context, intent);
    }

}

