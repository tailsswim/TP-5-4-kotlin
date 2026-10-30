
package com.example.batterymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ChargingReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "charging_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {

            createChannel(context);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(
                                    android.R.drawable.ic_lock_idle_charging)
                            .setContentTitle("Téléphone en charge")
                            .setContentText(
                                    "Votre téléphone est maintenant en charge."
                            )
                            .setPriority(
                                    NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(
                            Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.notify(202, builder.build());
            }
        }
    }

    private void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Charging Notifications",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
}
