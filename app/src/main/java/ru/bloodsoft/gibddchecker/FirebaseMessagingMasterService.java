package ru.bloodsoft.gibddchecker;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yandex.metrica.push.firebase.MetricaMessagingService;

public class FirebaseMessagingMasterService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        // AppMetrica automatically recognizes its messages and processes them only.
        new MetricaMessagingService().processPush(this, message);

        // Implement the logic for sending messages to other SDKs.
    }
}