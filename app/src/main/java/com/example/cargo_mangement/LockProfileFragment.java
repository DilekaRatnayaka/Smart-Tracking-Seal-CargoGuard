package com.example.cargo_mangement;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LockProfileFragment extends Fragment {

    private Button btnTrackTheTruck;
    private TextView lockStatusTextView;
    private TextView driverNameTextView;
    private TextView truckNumberTextView;
    private TextView contentsTextView;
    private TextView weightTextView;

    private FirebaseFirestore db;
    private Handler handler;
    private final int delay = 1 * 60 * 1000; // 1 minute in milliseconds
    private NotificationManager notificationManager;
    private String previousLockStatus;
    private String lockId;
    private String userDocumentId;
    private String journeyCollection;

    private static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "lock_status_channel";
    private static final String TAG = "LockProfile";

    float x1, x2, y1, y2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock_profile, container, false);

        db = FirebaseFirestore.getInstance();

        // Retrieve the lock ID, user document ID, and journey collection name passed from the previous activity
        if (getArguments() != null) {
            lockId = getArguments().getString("lock_id");
            userDocumentId = getArguments().getString("user_document_id");
            journeyCollection = getArguments().getString("journey_collection");
        }

        // Initialize Handler
        handler = new Handler(Looper.getMainLooper());

        // Initialize NotificationManager
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel (for Android Oreo and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        // Initialize UI elements
        driverNameTextView = view.findViewById(R.id.driverNameTextView);
        truckNumberTextView = view.findViewById(R.id.vehicleNumberTextView);
        contentsTextView = view.findViewById(R.id.contentsTextView);
        weightTextView = view.findViewById(R.id.weightTextView);

        btnTrackTheTruck = view.findViewById(R.id.btn_TrackTheTruck);
        lockStatusTextView = view.findViewById(R.id.lockStatusTextView);

        // Fetch and display lock status and additional details
        fetchLatestJourneyCollectionAndDetails();

        // Schedule the task to run every minute
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchLatestJourneyCollectionAndDetails();
                handler.postDelayed(this, delay);
            }
        }, delay);

        btnTrackTheTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToMapFragment();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent touchEvent) {
                switch (touchEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = touchEvent.getX();
                        y1 = touchEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = touchEvent.getX();
                        y2 = touchEvent.getY();
                        float deltaX = x2 - x1;
                        float deltaY = y2 - y1;
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            if (x1 < x2) {
                                navigateToTruckTrackingFragment();
                            } else if (x1 > x2) {
                                navigateToTruckTrackingFragment();
                            }
                        }
                        break;
                }
                return true;
            }
        });

        return view;
    }

    private void fetchLatestJourneyCollectionAndDetails() {
        // Fetch the latest journey collection
        db.collection("users")
                .document(userDocumentId)
                .collection("Locks")
                .document(lockId)
                .collection("Journeys")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                String latestJourneyCollection = getLatestJourneyCollection(querySnapshot);
                                if (latestJourneyCollection != null) {
                                    fetchDataFromFirestoreAndShowStatus(latestJourneyCollection);
                                    fetchAdditionalDetailsFromFirestore(latestJourneyCollection);
                                } else {
                                    Log.d(TAG, "No valid journey collections found.");
                                }
                            } else {
                                Log.d(TAG, "No journey collections found.");
                            }
                        } else {
                            Log.d(TAG, "Error getting journey collections: ", task.getException());
                        }
                    }
                });
    }

    private String getLatestJourneyCollection(QuerySnapshot querySnapshot) {
        String latestCollection = null;
        int maxJourneyNumber = 0;

        for (QueryDocumentSnapshot document : querySnapshot) {
            String docId = document.getId();
            if (docId.startsWith("Journey")) {
                int journeyNumber = Integer.parseInt(docId.replace("Journey", ""));
                if (journeyNumber > maxJourneyNumber) {
                    maxJourneyNumber = journeyNumber;
                    latestCollection = docId;
                }
            }
        }

        return latestCollection;
    }

    private void fetchDataFromFirestoreAndShowStatus(String journeyCollection) {
        db.collection("users")
                .document(userDocumentId)
                .collection("Locks")
                .document(lockId)
                .collection(journeyCollection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot lastDocument = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                                if (lastDocument.contains("belt")) {
                                    String beltValueStr = lastDocument.getString("belt");
                                    if (beltValueStr != null) {
                                        int beltValue = Integer.parseInt(beltValueStr);
                                        String lockStatus = beltValue == 33 ? "Locked" : "Unlocked";
                                        updateLockStatusUI(lockStatus);
                                    } else {
                                        Log.d(TAG, "Belt value is null");
                                    }
                                } else {
                                    Log.d(TAG, "Document does not contain 'belt' field");
                                }
                            } else {
                                Log.d(TAG, "No documents found in the collection");
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fetchAdditionalDetailsFromFirestore(String journeyCollection) {
        db.collection("users")
                .document(userDocumentId)
                .collection("Locks")
                .document(lockId)
                .collection(journeyCollection)
                .document("Details")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String driverName = document.getString("driverName");
                                String truckNumber = document.getString("truckNumber");
                                String contents = document.getString("contents");
                                Long weightLong = document.getLong("weight");
                                if (weightLong != null) {
                                    String weight = String.valueOf(weightLong);
                                    updateAdditionalDetails(driverName, truckNumber, contents, weight);
                                } else {
                                    Log.d(TAG, "Weight is null");
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void updateAdditionalDetails(String driverName, String truckNumber, String contents, String weight) {
        driverNameTextView.setText("Driver's Name: " + driverName);
        truckNumberTextView.setText("Vehicle Number: " + truckNumber);
        contentsTextView.setText("Contents: " + contents);
        weightTextView.setText("Weight in kg: " + weight);
    }

    private void updateLockStatusUI(String lockStatus) {
        lockStatusTextView.setText("Lock Status: " + lockStatus);
        if (!lockStatus.equals(previousLockStatus)) {
            showNotification(lockStatus);
            previousLockStatus = lockStatus;
        }
    }

    private void showNotification(String lockStatus) {
        RemoteViews notificationLayout = new RemoteViews(getActivity().getPackageName(), R.layout.notification);
        notificationLayout.setTextViewText(R.id.notification_content, "Cargo lock status: " + lockStatus);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomContentView(notificationLayout)
                .setContentText("Cargo lock status: " + lockStatus)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Cargo lock status: " + lockStatus));

        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void navigateToMapFragment() {
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString("user_document_id", userDocumentId);
        args.putString("lock_id", lockId);
        args.putString("journey_collection", journeyCollection);
        mapFragment.setArguments(args);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToTruckTrackingFragment() {
        TruckTrackingFragment truckTrackingFragment = new TruckTrackingFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, truckTrackingFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
