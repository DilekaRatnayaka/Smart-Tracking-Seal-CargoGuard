package com.example.cargo_mangement;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.widget.Toast;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.firebase.auth.FirebaseAuth;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private MeowBottomNavigation meowBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the HomeFragment by default
        loadFragment(new HomeFragment());

        MeowBottomNavigation meowBottomNavigation = findViewById(R.id.bottomNavigation);

        meowBottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.truck_icon));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.truck_tracking_icon));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.home_icon));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.logout_icon));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.support_icon));

        meowBottomNavigation.show(3, true);

        meowBottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()) {
                    case 1:
                        replaceFragment(new CargomanagementFragment());
                        break;
                    case 2:
                        replaceFragment(new TruckTrackingFragment());
                        break;
                    case 3:
                        replaceFragment(new HomeFragment());
                        break;
                    case 4:
                        showLogoutDialog(); // Show logout confirmation dialog
                        break;
                    case 5:
                        replaceFragment(new SupportFragment());
                        break;
                }
                return null;
            }
        });
        // if user repeat to click on an item menu
        meowBottomNavigation.setOnReselectListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                Toast.makeText(MainActivity.this, "You are Already Here!", Toast.LENGTH_SHORT).show();
                return null;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        // Create a new FragmentTransaction to replace the FrameLayout with the HomeFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.logout_dialog_box);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize buttons from dialog layout
        dialog.findViewById(R.id.btnCancel).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                dialog.dismiss(); // Dismiss dialog on Cancel button click
            }
        });

        dialog.findViewById(R.id.btnLogout).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                // Perform logout action here
                FirebaseAuth.getInstance().signOut(); // Sign out from Firebase
                Toast.makeText(MainActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // Set the logout flag
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_logged_out", true)
                        .apply();

                // Clear other local login state (shared preferences or similar storage)
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .remove("other_login_state_key")
                        .apply();

                // Redirect to LoginActivity and clear back stack
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Finish MainActivity
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }
}
