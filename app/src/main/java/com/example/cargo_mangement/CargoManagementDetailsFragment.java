package com.example.cargo_mangement;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CargoManagementDetailsFragment extends Fragment {

    private EditText editTextDriverName, editTextTruckNumber, editTextContents, editTextWeight;
    private Button buttonInitiateCargoLoading;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "CargoManagementDetailsActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_cargo_management_details, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Retrieve the lock ID and user document ID passed from the previous activity
        String lockId = getArguments().getString("lock_id");
        String userDocumentId = getArguments().getString("user_document_id");

        // Initialize views
        editTextDriverName = view.findViewById(R.id.editTextDriverName);
        editTextTruckNumber = view.findViewById(R.id.editTextTruckNumber);
        editTextContents = view.findViewById(R.id.editTextContents);
        editTextWeight = view.findViewById(R.id.editTextWeight);
        buttonInitiateCargoLoading = view.findViewById(R.id.buttonInitiateCargoLoading);

        buttonInitiateCargoLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNextJourneyCollection(lockId, userDocumentId); // Pass lockId and userDocumentId to the createNextJourneyCollection method
            }
        });

        return view;
    }

    // Method to create the next journey collection
    private void createNextJourneyCollection(String userDocumentId, String lockId) {
        db.collection("users")
                .document(userDocumentId)
                .collection("Locks")
                .document(lockId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Get the number of existing Journey collections
                                int journeyCount = document.getData().size();

                                // Create the new Journey collection
                                String newJourney = "Journey" + (journeyCount + 1);
                                db.collection("users")
                                        .document(userDocumentId)
                                        .collection("Locks")
                                        .document(lockId)
                                        .collection(newJourney)
                                        .document("Details")
                                        .set(new HashMap<>()) // You can set initial data if needed
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Journey collection created successfully");
                                                // Save cargo data after creating the journey
                                                saveCargoDataToFirestore(lockId, userDocumentId, newJourney);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Error creating journey collection", e);
                                                // Handle failure, if needed
                                            }
                                        });
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void saveCargoDataToFirestore(String lockId, String userDocumentId, String journeyCollection) {
        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Construct data to be saved
            String driverName = editTextDriverName.getText().toString().trim();
            String truckNumber = editTextTruckNumber.getText().toString().trim();
            String contents = editTextContents.getText().toString().trim();
            String weightString = editTextWeight.getText().toString().trim();

            // Check if any field is empty
            if (TextUtils.isEmpty(driverName) || TextUtils.isEmpty(truckNumber) || TextUtils.isEmpty(contents) || TextUtils.isEmpty(weightString)) {
                // Display toast message if any field is empty
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double weight = Double.parseDouble(weightString);

            // Create a map to store the cargo details
            Map<String, Object> cargo = new HashMap<>();
            cargo.put("driverName", driverName);
            cargo.put("truckNumber", truckNumber);
            cargo.put("contents", contents);
            cargo.put("weight", weight);

            // Access Firestore and save cargo data under the user's document, lock document, and journey document
            DocumentReference journeyRef = db.collection("users")
                    .document(userDocumentId) // Use the provided userDocumentId here
                    .collection("Locks")
                    .document(lockId) // Use the provided lockId here
                    .collection(journeyCollection)
                    .document("Details");

            journeyRef.set(cargo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Data saved successfully
                                Toast.makeText(getActivity(), "Cargo details saved successfully", Toast.LENGTH_SHORT).show();
                                // Clear the fields
                                editTextDriverName.setText("");
                                editTextTruckNumber.setText("");
                                editTextContents.setText("");
                                editTextWeight.setText("");
                            } else {
                                // Failed to save data
                                // Handle the error
                                Toast.makeText(getActivity(), "Failed to save cargo details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}