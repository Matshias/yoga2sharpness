package com.matshias.yoga2sharpnessfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class ScreenOnReceiver extends BroadcastReceiver {
    public ScreenOnReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            boolean setdisplayon = settings.getBoolean("checkDisplayOn", false);

            PowerManager pm = (PowerManager)
                    context.getSystemService(Context.POWER_SERVICE);

            boolean isScreenOn;
            if (android.os.Build.VERSION.SDK_INT < 20)
                isScreenOn = pm.isScreenOn();
            else
                isScreenOn = pm.isInteractive();

            if (isScreenOn && setdisplayon) {

                boolean cabc = settings.getBoolean("disableCABC", false);
                boolean pfit = settings.getBoolean("disablePFIT", false);
                if (cabc || pfit) {
                    BroadcastIntentService.startActionScreenOn(context, cabc, pfit);
                }
            }
        }
    }

}
