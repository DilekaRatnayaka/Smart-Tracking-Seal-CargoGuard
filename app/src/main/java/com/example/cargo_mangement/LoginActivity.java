package com.example.cargo_mangement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    boolean isPasswordVisible = false;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isJustLoggedOut = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("is_logged_out", false);

        if (currentUser != null && !isJustLoggedOut) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else if (isJustLoggedOut) {
            // Reset the flag after handling logout
            getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_out", false)
                    .apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        buttonLogin = findViewById(R.id.btn_login);
        TextView signUpLink = findViewById(R.id.tv_sign_up_link);

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Combine the TextViews for "Already have an account? Sign In"
        TextView signUpText = findViewById(R.id.tv_sign_up);
        String text = "Don't have an account? Sign Up";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        };

        // Make "Sign Up" clickable
        spannableString.setSpan(clickableSpan, 24, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpText.setText(spannableString);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());

        // Set a click listener for the "Login" button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    // Show a Toast message for empty fields and return
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Set up password visibility toggle
        TextInputLayout passwordTextInput = findViewById(R.id.PasswordTextInputLayout);
        if (passwordTextInput != null) {
            passwordTextInput.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            passwordTextInput.setEndIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePasswordVisibility(passwordTextInput);
                }
            });
        }
    }

    private void togglePasswordVisibility(TextInputLayout passwordTextInputLayout) {
        // Get the current input type of the password EditText
        int inputType = editTextPassword.getInputType();

        if (isPasswordVisible) {
            // Hide the password
            editTextPassword.setInputType(
                    inputType | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
        } else {
            // Show the password
            editTextPassword.setInputType(
                    inputType | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
        }

        // Update transformation method to reflect password visibility
        editTextPassword.setTransformationMethod(isPasswordVisible ?
                PasswordTransformationMethod.getInstance() :
                HideReturnsTransformationMethod.getInstance());

        // Move the cursor to the end of the input field to maintain cursor position
        editTextPassword.setSelection(editTextPassword.getText().length());

        // Update the visibility state
        isPasswordVisible = !isPasswordVisible;

        // Update the icon on TextInputLayout
        if (passwordTextInputLayout != null) {
            passwordTextInputLayout.setEndIconDrawable(isPasswordVisible ?
                    R.drawable.visibility_icon : R.drawable.visibility_off_icon);
        }
    }
}

