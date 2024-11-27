package com.agile.tuon;

import static com.agile.tuon.QuizFragment.currentScore;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;

import com.agile.tuon.QuizFragment;

public class ProgressFragment extends Fragment {
        private TextView wordsLearnedText, averageScoreText;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_progress, container, false);

            wordsLearnedText = view.findViewById(R.id.words_learned_text);
            averageScoreText = view.findViewById(R.id.average_score_text);

            loadStatistics();
            return view;
        }

        private void loadStatistics() {
            DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

            // Words learned today
            int wordsLearned = dbHelper.getWordsLearnedToday();
            wordsLearnedText.setText("Words learned today: " + currentScore);

            // Average quiz score
            double averageScore = dbHelper.getAverageQuizScore();
            averageScoreText.setText("Average quiz score: " + String.format(Locale.US, "%.2f", averageScore) + "%");

            // Vocabulary mastery
            /* double mastery = dbHelper.getVocabularyMastery(); */
        }


    }
