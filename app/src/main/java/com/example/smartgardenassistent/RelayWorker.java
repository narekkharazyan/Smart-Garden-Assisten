package com.example.smartgardenassistent;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RelayWorker extends Worker {

    public RelayWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Control relay based on the task
        controlRelay("ON");

        // Wait for a specific duration (can be configured) before turning off the relay
        try {
            Thread.sleep(60000); // sleep for 1 minute, modify as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }

        controlRelay("OFF");

        return Result.success();
    }

    private void controlRelay(String command) {
        DatabaseReference relayReference = FirebaseDatabase.getInstance().getReference().child("Node1").child("Relay");
        relayReference.setValue(command);
    }
}
