package com.agile.tuon;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

import com.agile.tuon.FlashcardsFragment;
import com.agile.tuon.QuizFragment;
import com.agile.tuon.ProgressFragment;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        initializeTextToSpeech();
        setupBottomNavigation();

        // Load initial fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FlashcardsFragment())
                .commit();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set Filipino language (closest to Bisaya available in Android TTS)
                textToSpeech.setLanguage(new Locale("fil", "PH"));
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Using if-else instead of switch-case
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
            return true; // Indicate that the item selection was handled
        });
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
}