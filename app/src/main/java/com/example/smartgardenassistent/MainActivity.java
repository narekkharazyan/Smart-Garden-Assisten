package com.example.smartgardenassistent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView humidityTextView;
    private TextView temperatureTextView;
    private TextView wateringTextView;
    private LinearLayout timersLayout;
    private LinearLayout autoWateringSettings;
    private Button startWateringButton;
    private Button stopWateringButton;
    private Button addTimerButton;
    private Button setWateringMethodButton;

    private SeekBar humiditySeekBar;
    private SeekBar temperatureSeekBar;
    private SeekBar durationSeekBar;
    private TextView humidityValue;
    private TextView temperatureValue;
    private TextView durationValue;

    private List<Timer> timers;
    private Handler handler;
    private boolean isSettingStartTime;
    private boolean isAutoWatering = false;

    private int humidityThreshold = 50;
    private int temperatureThreshold = 30;
    private int wateringDuration = 10; // in minutes

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        humidityTextView = findViewById(R.id.humidityTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        wateringTextView = findViewById(R.id.wateringTextView);
        timersLayout = findViewById(R.id.timersLayout);
        autoWateringSettings = findViewById(R.id.autoWateringSettings);
        startWateringButton = findViewById(R.id.startWateringButton);
        stopWateringButton = findViewById(R.id.stopWateringButton);
        addTimerButton = findViewById(R.id.addTimerButton);
        setWateringMethodButton = findViewById(R.id.setWateringMethodButton);

        humiditySeekBar = findViewById(R.id.humiditySeekBar);
        temperatureSeekBar = findViewById(R.id.temperatureSeekBar);
        durationSeekBar = findViewById(R.id.durationSeekBar);
        humidityValue = findViewById(R.id.humidityValue);
        temperatureValue = findViewById(R.id.temperatureValue);
        durationValue = findViewById(R.id.durationValue);

        handler = new Handler();
        isSettingStartTime = true;
        timers = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Node1");

        // Load data from Firebase
        loadWateringMethod();
        loadTimers();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Float humidity = dataSnapshot.child("Humidity").getValue(Float.class);
                Float temperature = dataSnapshot.child("Temperature").getValue(Float.class);

                humidityTextView.setText("Humidity: " + humidity + "%");
                temperatureTextView.setText("Temperature: " + temperature + "°C");

                if (isAutoWatering && humidity < humidityThreshold && temperature < temperatureThreshold) {
                    controlRelay("ON");
                    displayMessage("Auto-watering started!");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            controlRelay("OFF");
                        }
                    }, wateringDuration * 60 * 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        startWateringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlRelay("ON");
            }
        });

        stopWateringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlRelay("OFF");
            }
        });

        addTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        setWateringMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWateringMethodDialog();
            }
        });

        humiditySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                humidityThreshold = progress;
                humidityValue.setText(progress + "%");
                saveAutoSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        temperatureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                temperatureThreshold = progress;
                temperatureValue.setText(progress + "°C");
                saveAutoSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wateringDuration = progress;
                durationValue.setText(progress + " minutes");
                saveAutoSettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void controlRelay(String command) {
        DatabaseReference relayReference = FirebaseDatabase.getInstance().getReference().child("Node1").child("Relay");

        relayReference.setValue(command);

        if (command.equals("ON")) {
            wateringTextView.setText("Watering started");
        } else {
            wateringTextView.setText("Watering stopped");
        }
    }

    private void displayMessage(String message) {
        wateringTextView.setText(message);
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (isSettingStartTime) {
                    Timer timer = new Timer();
                    timer.startHour = hourOfDay;
                    timer.startMinute = minute;
                    timers.add(timer);
                    isSettingStartTime = false;
                    showTimePickerDialog();
                } else {
                    Timer timer = timers.get(timers.size() - 1);
                    timer.stopHour = hourOfDay;
                    timer.stopMinute = minute;
                    isSettingStartTime = true;
                    displayTimers();
                    scheduleRelayControl(timer);
                    saveTimers();
                }
            }
        }, hour, minute, true);
        timePickerDialog.setTitle(isSettingStartTime ? "Set Start Time" : "Set Stop Time");
        timePickerDialog.show();
    }

    private void displayTimers() {
        timersLayout.removeAllViews();
        for (int i = 0; i < timers.size(); i++) {
            Timer timer = timers.get(i);
            LinearLayout timerLayout = new LinearLayout(this);
            timerLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView timerTextView = new TextView(this);
            timerTextView.setText(String.format("Timer %d: %02d:%02d - %02d:%02d", i + 1, timer.startHour, timer.startMinute, timer.stopHour, timer.stopMinute));
            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Timer")
                            .setMessage("Are you sure you want to delete this timer: " + String.format("%02d:%02d - %02d:%02d", timer.startHour, timer.startMinute, timer.stopHour, timer.stopMinute) + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    timers.remove(timer);
                                    displayTimers();
                                    saveTimers();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
            timerLayout.addView(timerTextView);
            timerLayout.addView(deleteButton);
            timersLayout.addView(timerLayout);
        }
    }

    private void scheduleRelayControl(Timer timer) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, timer.startHour);
        startCalendar.set(Calendar.MINUTE, timer.startMinute);
        startCalendar.set(Calendar.SECOND, 0);

        Calendar stopCalendar = Calendar.getInstance();
        stopCalendar.set(Calendar.HOUR_OF_DAY, timer.stopHour);
        stopCalendar.set(Calendar.MINUTE, timer.stopMinute);
        stopCalendar.set(Calendar.SECOND, 0);

        long startDelay = startCalendar.getTimeInMillis() - System.currentTimeMillis();
        long stopDelay = stopCalendar.getTimeInMillis() - System.currentTimeMillis();

        if (startDelay < 0) {
            startDelay += 24 * 60 * 60 * 1000; // Adjust for next day if time has passed for today
        }
        if (stopDelay < 0) {
            stopDelay += 24 * 60 * 60 * 1000; // Adjust for next day if time has passed for today
        }

        WorkRequest startWorkRequest = new OneTimeWorkRequest.Builder(RelayWorker.class)
                .setInitialDelay(startDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkRequest stopWorkRequest = new OneTimeWorkRequest.Builder(RelayWorker.class)
                .setInitialDelay(stopDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(startWorkRequest);
        WorkManager.getInstance(this).enqueue(stopWorkRequest);
    }

    private void showWateringMethodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Watering Method");
        builder.setItems(new String[]{"Auto Watering", "Timer"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        isAutoWatering = true;
                        saveWateringMethod(true);
                        addTimerButton.setVisibility(View.GONE);
                        timersLayout.setVisibility(View.GONE);
                        autoWateringSettings.setVisibility(View.VISIBLE);
                        wateringTextView.setText("Auto-watering enabled");
                        setWateringMethodButton.setText("Change Plant Watering Method");
                        break;
                    case 1:
                        isAutoWatering = false;
                        saveWateringMethod(false);
                        addTimerButton.setVisibility(View.VISIBLE);
                        timersLayout.setVisibility(View.VISIBLE);
                        autoWateringSettings.setVisibility(View.GONE);
                        wateringTextView.setText("Timer mode enabled");
                        setWateringMethodButton.setText("Change Plant Watering Method");
                        break;
                }
            }
        });
        builder.show();
    }

    private void saveWateringMethod(boolean isAutoWatering) {
        DatabaseReference wateringMethodRef = FirebaseDatabase.getInstance().getReference().child("Node1").child("WateringMethod");
        wateringMethodRef.setValue(isAutoWatering ? "AUTO" : "TIMER");
    }

    private void saveAutoSettings() {
        DatabaseReference autoSettingsRef = FirebaseDatabase.getInstance().getReference().child("Node1").child("AutoSettings");
        autoSettingsRef.child("HumidityThreshold").setValue(humidityThreshold);
        autoSettingsRef.child("TemperatureThreshold").setValue(temperatureThreshold);
        autoSettingsRef.child("WateringDuration").setValue(wateringDuration);
    }

    private void saveTimers() {
        DatabaseReference timersRef = FirebaseDatabase.getInstance().getReference().child("Node1").child("Timers");
        timersRef.setValue(timers);
    }

    private void loadWateringMethod() {
        DatabaseReference wateringMethodRef = FirebaseDatabase.getInstance().getReference().child("Node1").child("WateringMethod");
        wateringMethodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String method = dataSnapshot.getValue(String.class);
                if ("AUTO".equals(method)) {
                    isAutoWatering = true;
                    addTimerButton.setVisibility(View.GONE);
                    timersLayout.setVisibility(View.GONE);
                    autoWateringSettings.setVisibility(View.VISIBLE);
                    wateringTextView.setText("Auto-watering enabled");
                    setWateringMethodButton.setText("Change Plant Watering Method");
                    loadAutoSettings();
                } else {
                    isAutoWatering = false;
                    addTimerButton.setVisibility(View.VISIBLE);
                    timersLayout.setVisibility(View.VISIBLE);
                    autoWateringSettings.setVisibility(View.GONE);
                    wateringTextView.setText("Timer mode enabled");
                    setWateringMethodButton.setText("Change Plant Watering Method");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read watering method.", databaseError.toException());
            }
        });
    }

    private void loadAutoSettings() {
        DatabaseReference autoSettingsRef = FirebaseDatabase.getInstance().getReference().child("Node1").child("AutoSettings");
        autoSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    humidityThreshold = dataSnapshot.child("HumidityThreshold").getValue(Integer.class);
                    temperatureThreshold = dataSnapshot.child("TemperatureThreshold").getValue(Integer.class);
                    wateringDuration = dataSnapshot.child("WateringDuration").getValue(Integer.class);

                    humiditySeekBar.setProgress(humidityThreshold);
                    temperatureSeekBar.setProgress(temperatureThreshold);
                    durationSeekBar.setProgress(wateringDuration);

                    humidityValue.setText(humidityThreshold + "%");
                    temperatureValue.setText(temperatureThreshold + "°C");
                    durationValue.setText(wateringDuration + " minutes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read auto settings.", databaseError.toException());
            }
        });
    }

    private void loadTimers() {
        DatabaseReference timersRef = FirebaseDatabase.getInstance().getReference().child("Node1").child("Timers");
        timersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot timerSnapshot : dataSnapshot.getChildren()) {
                        Timer timer = timerSnapshot.getValue(Timer.class);
                        timers.add(timer);
                    }
                    displayTimers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read timers.", databaseError.toException());
            }
        });
    }

    public static class Timer {
        public int startHour;
        public int startMinute;
        public int stopHour;
        public int stopMinute;

        public Timer() {
            // Default constructor required for calls to DataSnapshot.getValue(Timer.class)
        }
    }
}
