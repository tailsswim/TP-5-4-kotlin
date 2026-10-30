
package com.example.batterymonitor;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

public class BatteryMonitoringService extends Service {

    public static final String LOW_BATTERY_ACTION =
            "com.example.batterymonitor.LOW_BATTERY_ALERT";

    private static final String CHANNEL_ID = "battery_service_channel";
    private static final int NOTIFICATION_ID = 200;

    private BroadcastReceiver batteryReceiver;
    private ChargingReceiver chargingReceiver;

    private boolean wasLow = false;
    private boolean receiversRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        // Notification permanente du service
        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_lock_idle_low_battery)
                        .setContentTitle("Battery Monitor")
                        .setContentText("Surveillance de la batterie active")
                        .setOngoing(true)
                        .build();

        startForeground(NOTIFICATION_ID, notification);

        registerBatteryReceiver();
        registerChargingReceiver();
    }

    private void registerBatteryReceiver() {

        batteryReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent == null) return;

                int level = intent.getIntExtra(
                        BatteryManager.EXTRA_LEVEL, -1);

                int scale = intent.getIntExtra(
                        BatteryManager.EXTRA_SCALE, 100);

                if (level < 0 || scale <= 0) return;

                int batteryPercentage = (level * 100) / scale;

                int threshold = getSharedPreferences(
                        "BatterySettings",
                        MODE_PRIVATE
                ).getInt("threshold", 20);

                boolean isLow = batteryPercentage <= threshold;

                // Envoyer l'alerte uniquement au passage
                // sous le seuil pour éviter les notifications répétées.
                if (isLow && !wasLow) {

                    Intent alert = new Intent(
                            BatteryMonitoringService.LOW_BATTERY_ACTION
                    );

                    alert.setClass(
                            BatteryMonitoringService.this,
                            LowBatteryReceiver.class
                    );

                    alert.putExtra("battery_level", batteryPercentage);

                    sendBroadcast(alert);
                }

                wasLow = isLow;
            }
        };

        IntentFilter filter =
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        // ACTION_BATTERY_CHANGED est un Broadcast sticky.
        registerReceiver(batteryReceiver, filter);
    }

    private void registerChargingReceiver() {

        chargingReceiver = new ChargingReceiver();

        IntentFilter filter =
                new IntentFilter(Intent.ACTION_POWER_CONNECTED);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                    chargingReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(chargingReceiver, filter);
        }

        receiversRegistered = true;
    }

    private void registerReceiver(ChargingReceiver chargingReceiver, IntentFilter filter) {
    }

    private void registerReceiver(ChargingReceiver chargingReceiver, IntentFilter filter, int receiverNotExported) {
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Battery Monitoring",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {

        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }

        if (receiversRegistered && chargingReceiver != null) {
            unregisterReceiver(chargingReceiver);
        }

        stopForeground(true);

        super.onDestroy();
    }

    private void unregisterReceiver(ChargingReceiver chargingReceiver) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
