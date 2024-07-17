package com.example.cargo_mangement;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MapFragment extends Fragment {

    private SupportMapFragment supportMapFragment;
    private FirebaseFirestore db;
    private GoogleMap googleMap;
    private Handler handler;
    private static final long UPDATE_INTERVAL = 1 * 60 * 1000; // 1 minute in milliseconds
    private String userDocumentId;
    private String lockId;
    private String journeyCollection;

    private float x1, x2, y1, y2;

    private static final String TAG = "MapFragment";
    private int locationCounter = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_map, container, false);

        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        db = FirebaseFirestore.getInstance();
        handler = new Handler(Looper.getMainLooper());

        // Retrieve userDocumentId and lockId from arguments
        if (getArguments() != null) {
            userDocumentId = getArguments().getString("user_document_id");
            lockId = getArguments().getString("lock_id");
            journeyCollection = getArguments().getString("journey_collection");
        }

        Dexter.withContext(getContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        initMap();
                        fetchDataFromFirestoreAndShowOnMap(lockId, userDocumentId, journeyCollection);
                        scheduleFirestoreDataFetch();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        // Handle permission denied
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

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
                        // Check if the swipe is horizontal
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            // Horizontal swipe
                            if (x1 < x2) {
                                // Right swipe
                                navigateToLockProfileFragment();
                            } else if (x1 > x2) {
                                // Left swipe
                                navigateToLockProfileFragment();
                            }
                        }
                        break;
                }
                return true;
            }
        });

        return view;
    }

    private void scheduleFirestoreDataFetch() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Clear the map
                googleMap.clear();
                // Fetch all locations again from Firestore and show on map
                fetchDataFromFirestoreAndShowOnMap(lockId, userDocumentId, journeyCollection );
                // Reschedule for the next update
                scheduleFirestoreDataFetch();
            }
        }, UPDATE_INTERVAL);
    }

    private void initMap() {
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap map) {

                googleMap = map;
            }
        });
    }

    private void fetchDataFromFirestoreAndShowOnMap(String lockId, String userDocumentId, String journeyCollection ) {
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
                            LatLng lastLocation = null;
                            // Reset location counter
                            locationCounter = 1;

                            // Clear the map
                            googleMap.clear();

                            // Flag to check if GPS connection is lost
                            boolean isGPSCoordinatesValid = true;

                            // Loop through documents to add historical locations
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String latitudeStr = document.getString("latitude");
                                String longitudeStr = document.getString("longitude");

                                if (latitudeStr != null && longitudeStr != null &&
                                        !latitudeStr.equals("0") && !longitudeStr.equals("0")) {
                                    double latitude = Double.parseDouble(latitudeStr.trim());
                                    double longitude = Double.parseDouble(longitudeStr.trim());
                                    LatLng latLng = new LatLng(latitude, longitude);

                                    // Add historical location markers with red color and naming convention
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title("Location " + locationCounter)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                    // Update the last location for the current iteration
                                    lastLocation = latLng;

                                    locationCounter++;
                                } else {
                                    // Check if it's the last document
                                    if (task.getResult().size() > 0 && document.equals(task.getResult().getDocuments().get(task.getResult().size() - 1))) {
                                        // GPS connection lost, set the flag to false
                                        isGPSCoordinatesValid = false;
                                    }
                                }
                            }

                            // Display toast message if GPS coordinates are invalid
                            if (!isGPSCoordinatesValid) {
                                showToast("GPS connection lost");
                            }

                            // Add the last location marker with green color and "Current Location" name
                            if (lastLocation != null) {
                                googleMap.addMarker(new MarkerOptions()
                                        .position(lastLocation)
                                        .title("Current Location")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                                // Zoom into the last location
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLockProfileFragment() {
        // Assuming you're using a FragmentTransaction to switch between fragments
        LockProfileFragment lockProfileFragment = new LockProfileFragment();
        Bundle args = new Bundle();
        args.putString("lock_id", lockId);
        args.putString("user_document_id", userDocumentId);
        args.putString("journey_collection", journeyCollection);
        lockProfileFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, lockProfileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}