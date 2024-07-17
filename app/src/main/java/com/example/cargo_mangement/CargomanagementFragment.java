package com.example.cargo_mangement;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CargomanagementFragment extends Fragment implements ItemAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private List<Item> itemList;
    private ItemAdapter itemAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cargomanagement, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this);
        recyclerView.setAdapter(itemAdapter);

        fetchItemsFromFirestore();

        return rootView;
    }

    private void fetchItemsFromFirestore() {
        String userEmail = mAuth.getCurrentUser().getEmail(); // Get the email of the currently logged-in user

        db.collection("users")
                .whereEqualTo("email", userEmail) // Query documents where the email field matches the current user's email
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String userId = document.getId(); // Get the user document ID (e.g., U01)
                                // Now fetch documents from the "Locks" collection under the user's document
                                db.collection("users").document(userId).collection("Locks")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot lockDocument : task.getResult()) {
                                                        String lockId = lockDocument.getId(); // Get the lock document ID (e.g., D001)
                                                        itemList.add(new Item(lockId));
                                                    }
                                                    itemAdapter.notifyDataSetChanged();
                                                } else {
                                                    Toast.makeText(getContext(), "Failed to fetch locks", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to fetch user document", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onItemClick(Item item) {
        // Handle item click event and navigate to the lock profile page
        getCurrentUserDocumentID(item);
    }

    private void getCurrentUserDocumentID(Item item) {
        String userEmail = mAuth.getCurrentUser().getEmail();

        // Query the Firestore collection "users" to find the document ID where the email field matches the current user's email
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // If the query is successful and documents are found, extract the document ID
                            DocumentSnapshot document = task.getResult().getDocuments().get(0); // Assuming there's only one document
                            String userId = document.getId();
                            // Pass the document ID to the next fragement
                            navigateToCargoManagementDetails(item, userId);
                        } else {
                            // Handle the case where the query doesn't return any documents or is unsuccessful
                            Toast.makeText(getContext(), "Failed to fetch user document", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToCargoManagementDetails(Item item, String userId) {
        // Create a new instance of CargoManagementDetailsFragment
        CargoManagementDetailsFragment fragment = new CargoManagementDetailsFragment();

        // Pass the lock ID and current user's user document ID to the CargoManagementDetailsFragment using a Bundle
        Bundle args = new Bundle();
        args.putString("lock_id", item.getItemName()); // Assuming getItemName() returns the lock ID
        args.putString("user_document_id", userId); // Pass the current user's user document ID
        fragment.setArguments(args);

        // Replace the current fragment with CargoManagementDetailsFragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
