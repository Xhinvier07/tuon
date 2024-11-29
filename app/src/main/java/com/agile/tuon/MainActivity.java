package com.agile.tuon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        dbHelper = new DatabaseHelper(this);
        initializeTextToSpeech();
        setupBottomNavigation();

        // Load initial fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FlashcardsFragment())
                .commit();

        handleNotificationIntent(getIntent());
        rescheduleNotification();
    }


    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("fil", "PH"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech.setLanguage(Locale.ENGLISH); // Fallback to English
                    Toast.makeText(this, "Filipino TTS not supported, using English", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "TTS Initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    protected void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            // Check if the current fragment is QuizFragment and if the quiz is ongoing
            if (currentFragment instanceof QuizFragment) {
                QuizFragment quizFragment = (QuizFragment) currentFragment;
                if (quizFragment.isQuizOngoing()) {
                    // Show a dialog to confirm navigation
                    showNavigationWarningDialog();
                    return false; // Prevent navigation
                }
            }

            // Regular fragment switching logic
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_flashcards) {
                selectedFragment = new FlashcardsFragment();
            } else if (item.getItemId() == R.id.nav_quiz) {
                selectedFragment = new QuizFragment();
            } else if (item.getItemId() == R.id.nav_progress) {
                selectedFragment = new ProgressFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    private void showNavigationWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quiz in Progress")
                .setMessage("You must finish or exit the quiz before navigating away. Are you sure you want to exit the quiz?")
                .setPositiveButton("Exit Quiz", (dialog, which) -> {
                    // Allow navigation by replacing with a new fragment (optional)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FlashcardsFragment())
                            .commit();
                })
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    public TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            // Handle notification action
            String action = intent.getAction();
            if (action.equals("com.agile.tuon.NOTIFICATION_ACTION")) {
                // Handle notification action
                String message = intent.getStringExtra("message");
                }
        }
    }

    private void rescheduleNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences("TuonPrefs", Context.MODE_PRIVATE);
        int intervalHours = sharedPreferences.getInt("notificationInterval", 24);
        NotificationHelper.scheduleNotification(this, intervalHours);
    }
}