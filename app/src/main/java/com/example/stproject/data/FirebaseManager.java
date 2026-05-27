package com.example.stproject.data;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
    // This class can be used to manage Firebase instances
    // Standard initialization is handled automatically by the google-services plugin

    public static FirebaseFirestore getDb() {
        return FirebaseFirestore.getInstance();
    }
}
