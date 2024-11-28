package com.agile.tuon;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

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
    private ImageButton playPronunciationButton;
    private MediaPlayer mediaPlayer;
    private LinearLayout commonPhrasesContainer;
    private Handler handler = new Handler();
    private TextView proficiencyLevelTextView;
    private static final int WORDS_TARGET = 100;

    private String[][] commonPhrases = {
            {"Maayong Buntag", "Good Morning"},
            {"Wala ko kasabot","I don't understand" },
            {"Kumusta", "How are you"},
            {"Walay Sapayan", "You're Welcome"},
            {"Amping", "Take care"},
            {"Unsa Imong Pangalan", "Good Evening"},
            {"Pila imong edad", "How old are you"}
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        dbHelper = new DatabaseHelper(getContext());

        wordsLearnedTextView = view.findViewById(R.id.words_learned_text_view);
        streakDaysTextView = view.findViewById(R.id.streak_days_text_view);
        proficiencyLevelTextView = view.findViewById(R.id.proficiency_level_text_view);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        wordOfTheDayTextView = view.findViewById(R.id.word_of_the_day_text_view);
        wordOfTheDayTranslationTextView = view.findViewById(R.id.word_of_the_day_translation_text_view);
        playPronunciationButton = view.findViewById(R.id.play_pronunciation_button);
        commonPhrasesContainer = view.findViewById(R.id.common_phrases_container);

        updateProgressDisplay();
        updateWordOfTheDay();
        setupCommonPhrases();

        return view;
    }

    private void updateProgressDisplay() {
        int[] progress = dbHelper.getProgress();
        int wordsLearned = progress[0];
        int streakDays = progress[1];
        String lastStudyDate = dbHelper.getLastStudyDate();

        wordsLearnedTextView.setText(String.format(Locale.getDefault(), "%d/%d words learned", wordsLearned, WORDS_TARGET));
        streakDaysTextView.setText(String.format(Locale.getDefault(), "%d day streak", streakDays));

        // Calculate progress percentage based on 100 words target
        int progressPercentage = (int) ((wordsLearned / (float) WORDS_TARGET) * 100);
        progressIndicator.setProgress(progressPercentage);

        // Update proficiency level
        String proficiencyLevel = getProficiencyLevel(wordsLearned);
        proficiencyLevelTextView.setText(proficiencyLevel);

        updateStreak(lastStudyDate);
    }

    private String getProficiencyLevel(int wordsLearned) {
        if (wordsLearned < 33) {
            return "BEGINNER";
        } else if (wordsLearned < 66) {
            return "INTERMEDIATE";
        } else {
            return "PROFESSIONAL";
        }
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
            setupAudioPronunciation(playPronunciationButton, null, wordOfTheDay[0]);
        }
    }

    private void setupCommonPhrases() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String[] phrase : commonPhrases) {
            View phraseView = inflater.inflate(R.layout.item_common_phrase, commonPhrasesContainer, false);
            TextView phraseTextView = phraseView.findViewById(R.id.phrase_text_view);
            TextView translationTextView = phraseView.findViewById(R.id.translation_text_view);
            ImageButton playButton = phraseView.findViewById(R.id.play_button);
            CircularProgressIndicator audioProgress = phraseView.findViewById(R.id.audio_progress);

            phraseTextView.setText(phrase[0]);
            translationTextView.setText(phrase[1]);
            setupAudioPronunciation(playButton, audioProgress, phrase[0]);

            commonPhrasesContainer.addView(phraseView);
        }
    }

    private void setupAudioPronunciation(ImageButton playButton, CircularProgressIndicator audioProgress, String word) {
        String audioFileName = word.toLowerCase().replace(" ", "_");
        int resId = getResources().getIdentifier(audioFileName, "raw", requireContext().getPackageName());

        if (resId != 0) {
            playButton.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(v -> playAudio(resId, playButton, audioProgress));
        } else {
            playButton.setVisibility(View.GONE);
        }
    }

    private void playAudio(int resId, ImageButton playButton, CircularProgressIndicator audioProgress) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(requireContext(), resId);
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
            playButton.setVisibility(View.VISIBLE);
            audioProgress.setVisibility(View.GONE);
        });

        playButton.setVisibility(View.GONE);
        audioProgress.setVisibility(View.VISIBLE);
        audioProgress.setProgress(0);

        mediaPlayer.start();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int progress = (int) ((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration() * 100);
                    audioProgress.setProgress(progress);
                    if (mediaPlayer.isPlaying()) {
                        handler.postDelayed(this, 100);
                    }
                }
            }
        });
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void updateProgressFromQuiz(int correctAnswers) {
        dbHelper.incrementWordsLearned(correctAnswers);
        updateProgressDisplay();
    }
}

