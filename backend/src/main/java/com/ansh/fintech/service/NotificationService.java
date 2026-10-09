package com.ansh.fintech.service;

import com.ansh.fintech.model.NotificationRecord;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Firestore firestore;

    public NotificationService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Creates a notification record in notifications/{id} inside a Firestore transaction.
     */
    public String sendNotificationInTransaction(Transaction transaction, String userId, String message) {
        String notifId = UUID.randomUUID().toString();
        DocumentReference notifRef = firestore.collection("notifications").document(notifId);

        NotificationRecord notif = new NotificationRecord(
                notifId,
                userId,
                message,
                false,
                System.currentTimeMillis()
        );

        transaction.set(notifRef, notif.toMap());
        log.info("Notification sent to userId={}: {}", userId, message);
        return notifId;
    }
}
