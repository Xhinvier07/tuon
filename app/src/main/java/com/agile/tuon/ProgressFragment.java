package com.agile.tuon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private TextView wordsLearnedTextView;
    private TextView streakDaysTextView;
    private CircularProgressIndicator progressIndicator;
    private TextView wordOfTheDayTextView;
    private TextView wordOfTheDayTranslationTextView;
    private Slider notificationIntervalSlider;
    private TextView notificationIntervalTextView;
    private Button applyNotificationSettingsButton;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "TuonPrefs";
    private static final String PREF_NOTIFICATION_INTERVAL = "notificationInterval";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        dbHelper = new DatabaseHelper(getContext());
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        wordsLearnedTextView = view.findViewById(R.id.words_learned_text_view);
        streakDaysTextView = view.findViewById(R.id.streak_days_text_view);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        wordOfTheDayTextView = view.findViewById(R.id.word_of_the_day_text_view);
        wordOfTheDayTranslationTextView = view.findViewById(R.id.word_of_the_day_translation_text_view);
        notificationIntervalSlider = view.findViewById(R.id.notification_interval_slider);
        notificationIntervalTextView = view.findViewById(R.id.notification_interval_text_view);
        applyNotificationSettingsButton = view.findViewById(R.id.apply_notification_settings_button);

        updateProgressDisplay();
        updateWordOfTheDay();
        setupNotificationSettings();
        setupNotificationSettings();


        return view;
    }

    private void updateProgressDisplay() {
        int[] progress = dbHelper.getProgress();
        int wordsLearned = progress[0];
        int streakDays = progress[1];
        String lastStudyDate = dbHelper.getLastStudyDate();

        wordsLearnedTextView.setText(String.format(Locale.getDefault(), "%d words learned", wordsLearned));
        streakDaysTextView.setText(String.format(Locale.getDefault(), "%d day streak", streakDays));

        // Assuming a goal of 1000 words
        int progressPercentage = (int) ((wordsLearned / 1000.0) * 100);
        progressIndicator.setProgress(progressPercentage);

        updateStreak(lastStudyDate);
    }



    private void updateStreak(String lastStudyDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        if (!today.equals(lastStudyDate)) {
            int[] progress = dbHelper.getProgress();
            int currentStreak = progress[1];
            int wordsLearned = progress[0];

            try {
                Date lastDate = sdf.parse(lastStudyDate);
                Date currentDate = sdf.parse(today);

                if (currentDate != null && lastDate != null) {
                    long difference = currentDate.getTime() - lastDate.getTime();
                    int daysBetween = (int) (difference / (1000 * 60 * 60 * 24));

                    if (daysBetween == 1) {
                        currentStreak++;
                    } else if (daysBetween > 1) {
                        currentStreak = 1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            dbHelper.updateProgress(wordsLearned, currentStreak, today);
            updateProgressDisplay();
        }
    }

    private void updateWordOfTheDay() {
        String[] wordOfTheDay = dbHelper.getWordOfTheDay();
        if (wordOfTheDay != null) {
            wordOfTheDayTextView.setText(wordOfTheDay[0]);
            wordOfTheDayTranslationTextView.setText(wordOfTheDay[1]);
        }
    }

    private void setupNotificationSettings() {
        int savedInterval = sharedPreferences.getInt(PREF_NOTIFICATION_INTERVAL, 24);
        notificationIntervalSlider.setValue(savedInterval);
        updateNotificationIntervalText(savedInterval);

        notificationIntervalSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateNotificationIntervalText((int) value);
        });

        applyNotificationSettingsButton.setOnClickListener(v -> {
            int intervalHours = (int) notificationIntervalSlider.getValue();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_NOTIFICATION_INTERVAL, intervalHours);
            editor.apply();

            NotificationHelper.scheduleNotification(requireContext(), intervalHours);
        });
    }

    private void updateNotificationIntervalText(int hours) {
        if (hours < 24) {
            notificationIntervalTextView.setText(String.format(Locale.getDefault(), "Remind me every %d hour(s)", hours));
        } else {
            int days = hours / 24;
            notificationIntervalTextView.setText(String.format(Locale.getDefault(), "Remind me every %d day(s)", days));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressDisplay();
        updateWordOfTheDay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // This method should be called from the QuizFragment or wherever the quiz is implemented
    public void updateProgressFromQuiz(int correctAnswers) {
        dbHelper.incrementWordsLearned(correctAnswers);
        updateProgressDisplay();
    }
}
