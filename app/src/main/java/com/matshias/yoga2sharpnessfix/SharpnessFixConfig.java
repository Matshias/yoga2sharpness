package com.matshias.yoga2sharpnessfix;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;



public class SharpnessFixConfig extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private PrefsFragment frag;
    private BackgroundService s;

    private void enableBootReceiver()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean boot = settings.getBoolean("checkStartOnBoot", false);

        // enable boot recevier
        ComponentName receiver = new ComponentName(context, BootCompleteReceiver.class);
        PackageManager pm = context.getPackageManager();

        if (boot) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private void disableBootReceiver()
    {
        // disable boot receiver
        ComponentName receiver = new ComponentName(context, BootCompleteReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }

    //@Override
    protected void onResume() {
        super.onResume();

        Intent intent= new Intent(this, BackgroundService.class);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);



        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("ScreenOnReceived", false);
        edit.commit();
    }

    //@Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            BackgroundService.MyBinder b = (BackgroundService.MyBinder) binder;
            s = b.getService();

        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        frag = new PrefsFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, frag)
                .commit();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF0040E0));

        enableBootReceiver();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        boolean cabc = settings.getBoolean("disableCABC", false);
        boolean pfit = settings.getBoolean("disablePFIT", false);

        // start service if its not started
        Intent i = new Intent(context, BackgroundService.class);
        context.startService(i);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("disableCABC"))
        {
            boolean cabc = sharedPreferences.getBoolean("disableCABC", false);

            s.startActionCABC(this, cabc);
        }
        else if (key.equals("disablePFIT"))
        {
            boolean pfit = sharedPreferences.getBoolean("disablePFIT", false);

            s.startActionPFIT(this, pfit);
        }
        else if (key.equals("checkStartOnBoot")) {
            enableBootReceiver();
        }
    }


    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference);
        }

    }

}
