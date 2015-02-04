package com.matshias.yoga2sharpnessfix;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class BackgroundService extends Service {
    private Binder mBinder = new MyBinder();
    private ScreenOnReceiver receiver = null;

    public BackgroundService() {
    }

    private void registerBroadcastReceiver() {

        if (receiver == null) {
            final IntentFilter theFilter = new IntentFilter();
            /** System Defined Broadcast */
            theFilter.addAction(Intent.ACTION_SCREEN_ON);

            receiver = new ScreenOnReceiver();
            registerReceiver(receiver, theFilter);
        }
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful

        registerBroadcastReceiver();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean cabc = settings.getBoolean("disableCABC", false);
        boolean pfit = settings.getBoolean("disablePFIT", false);

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn;
        if (android.os.Build.VERSION.SDK_INT < 20)
            isScreenOn = pm.isScreenOn();
        else
            isScreenOn = pm.isInteractive();

        if (isScreenOn) {
            startActionScreenOn(this, cabc, pfit);
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    /**
     * Starts this service to perform action CABC with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public void startActionCABC(Context context, boolean disable) {
        BroadcastIntentService.startActionCABC(context, disable);
    }

    /**
     * Starts this service to perform action PFIT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public void startActionPFIT(Context context, boolean disable) {
        BroadcastIntentService.startActionPFIT(context, disable);
    }


    /**
     * Starts this service to perform action PFIT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public void startActionScreenOn(Context context, boolean cabc, boolean pfit) {
        BroadcastIntentService.startActionScreenOn(context, cabc, pfit);
    }
}
