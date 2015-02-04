package com.matshias.yoga2sharpnessfix;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BroadcastIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CABC = "com.example.matshias.yoga2sharpnessfix.action.CABC";
    private static final String ACTION_PFIT = "com.example.matshias.yoga2sharpnessfix.action.PFIT";
    private static final String ACTION_SCREEN_ON = Intent.ACTION_SCREEN_ON;

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.matshias.yoga2sharpnessfix.extra.CABC_DISABLE";
    private static final String EXTRA_PARAM2 = "com.example.matshias.yoga2sharpnessfix.extra.PFIT_DISABLE";
    private static final String EXTRA_PARAM3 = "com.example.matshias.yoga2sharpnessfix.extra.DELAY";

    /**
     * Starts this service to perform action CABC with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCABC(Context context, boolean disable) {
        Intent intent = new Intent(context, BroadcastIntentService.class);
        intent.setAction(ACTION_CABC);
        intent.putExtra(EXTRA_PARAM1, disable);
        intent.putExtra(EXTRA_PARAM3, 0);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action PFIT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPFIT(Context context, boolean disable) {
        Intent intent = new Intent(context, BroadcastIntentService.class);
        intent.setAction(ACTION_PFIT);
        intent.putExtra(EXTRA_PARAM2, disable);
        intent.putExtra(EXTRA_PARAM3, 0);
        context.startService(intent);
    }


    /**
     * Starts this service to perform action PFIT with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScreenOn(Context context, boolean cabc, boolean pfit) {
        Intent intent = new Intent(context, BroadcastIntentService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(ACTION_SCREEN_ON);
        intent.putExtra(EXTRA_PARAM1, cabc);
        intent.putExtra(EXTRA_PARAM2, pfit);
        intent.putExtra(EXTRA_PARAM3, 500);
        context.startService(intent);
    }

    public BroadcastIntentService() {
        super("BroadcastIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CABC.equals(action)) {
                final boolean disable = intent.getBooleanExtra(EXTRA_PARAM1, false);
                handleActionCABC(disable);
            } else if (ACTION_PFIT.equals(action)) {
                final boolean disable = intent.getBooleanExtra(EXTRA_PARAM2, false);
                handleActionPFIT(disable);
            } else if (ACTION_SCREEN_ON.equals(action)) {
                final boolean disableCabc = intent.getBooleanExtra(EXTRA_PARAM1, false);
                final boolean disablePfit = intent.getBooleanExtra(EXTRA_PARAM2, false);
                final int delay = intent.getIntExtra(EXTRA_PARAM3, 0);

                // wait a bit because the driver might not be ready when receiving ACTION_SCREEN_ON
                SystemClock.sleep(delay);

                handleActionCABC(disableCabc);
                handleActionPFIT(disablePfit);
            }
        }
    }

    private void handleActionCABC(boolean disable) {
        String cmd;
        List<String> suResult;
        boolean suAvailable;

        if (disable) {
            cmd = "echo 0 > /sys/lcd_panel/cabc_onoff";
        } else {
            cmd = "echo 1 > /sys/lcd_panel/cabc_onoff";
        }

        suAvailable = Shell.SU.available();
        if (suAvailable) {
            //suVersion = Shell.SU.version(false);
            //suVersionInternal = Shell.SU.version(true);
            suResult = Shell.SU.run(new String[]{
                    cmd,

            });
        }

    }

    private void handleActionPFIT(boolean disable) {
        String cmd, cmd2;
        List<String> suResult;
        boolean suAvailable;

        if (disable)
        {
            System.loadLibrary("lenovo_display_fix");
            setPFIT();
        }
    }

    public native String  setPFIT();

}
