package com.example.cargo_mangement;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.content.Intent;
import androidx.annotation.NonNull;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText editTextFullName, editTextEmail, editTextPassword, editTextReEnterPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextFullName = findViewById(R.id.fullName);
        editTextEmail=findViewById(R.id.Email);
        editTextPassword=findViewById(R.id.Password);
        editTextReEnterPassword= findViewById(R.id.ReEnterPassword);
        buttonReg = findViewById(R.id.btn_register);
        TextView signInLink= findViewById(R.id.tv_sign_in_link);

        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        // Combine the TextViews for "Already have an account? Sign In"
        TextView signInText = findViewById(R.id.tv_sign_in);
        String text = "Already have an account? Sign In";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, 24, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signInText.setText(spannableString);
        signInText.setMovementMethod(LinkMovementMethod.getInstance());

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName, email,password, ReEnterPassword;
                fullName= editTextFullName.getText().toString().trim();
                email= editTextEmail.getText().toString().trim();
                password= editTextPassword.getText().toString().trim();
                ReEnterPassword= editTextReEnterPassword.getText().toString().trim();

                if (fullName.isEmpty()||  email.isEmpty() || password.isEmpty() || ReEnterPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!password.equals(ReEnterPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                else {
                    // Registration successful
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(RegisterActivity.this, "Authentication Successful.",
                                                Toast.LENGTH_SHORT).show();
                                        // Generate document ID and create Firestore collection
                                        generateDocumentId(fullName, email);
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegisterActivity.this, "error " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        // Log the exception for debugging purposes
                                        Log.e(TAG, "Authentication failed", task.getException());
                                    }
                                }
                            });
                }
            }
        });
    }
    private void createFirestoreCollection(String documentId, String fullName, String email) {
        // Create a new document in the 'users' collection with the custom ID
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        // Add more user details as needed

        // Add a new document with a custom ID
        db.collection("users")
                .document(documentId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User document added successfully");
                        } else {
                            Log.w(TAG, "Error adding user document", task.getException());
                        }
                    }
                });
    }

    // Generate a custom document ID
    // Generate a custom document ID and create Firestore collection
    private void generateDocumentId(String fullName, String email) {
        // Query Firestore to get the count of existing documents
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            // Generate the document ID based on the count
                            String documentId = "U" + String.format("%02d", count + 1);
                            // Once the ID is generated, call the method to create the document
                            createFirestoreCollection(documentId, fullName, email);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

}