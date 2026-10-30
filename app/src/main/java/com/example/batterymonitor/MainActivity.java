
package com.example.batterymonitor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tp54.R;

public class MainActivity extends Activity {

    private TextView serviceStatus;
    private EditText thresholdInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceStatus = findViewById(R.id.serviceStatus);
        thresholdInput = findViewById(R.id.thresholdInput);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        // Demander la permission Android 13+
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    100
            );
        }

        // Démarrer le service
        startButton.setOnClickListener(v -> {

            String value = thresholdInput.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(this,
                        "Veuillez saisir un seuil",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int threshold;

            try {
                threshold = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                threshold = -1;
            }

            if (threshold < 1 || threshold > 100) {
                Toast.makeText(this,
                        "Le seuil doit être entre 1 et 100",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Sauvegarder le seuil
            getSharedPreferences(
                    "BatterySettings",
                    MODE_PRIVATE
            ).edit().putInt("threshold", threshold).apply();

            // Démarrer le service
            Intent serviceIntent =
                    new Intent(this, BatteryMonitoringService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            serviceStatus.setText("Service démarré");

            Toast.makeText(this,
                    "Surveillance de la batterie activée",
                    Toast.LENGTH_SHORT).show();
        });

        // Arrêter le service
        stopButton.setOnClickListener(v -> {

            Intent serviceIntent =
                    new Intent(this, BatteryMonitoringService.class);

            stopService(serviceIntent);

            serviceStatus.setText("Service arrêté");

            Toast.makeText(this,
                    "Surveillance arrêtée",
                    Toast.LENGTH_SHORT).show();
        });
    }
}
