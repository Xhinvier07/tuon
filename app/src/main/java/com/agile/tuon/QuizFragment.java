package com.agile.tuon;

import com.agile.tuon.DatabaseHelper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizFragment extends Fragment {
    private TextView questionTextView;
    private RadioGroup optionsRadioGroup;
    private Button submitButton;
    private TextView scoreTextView;
    private TextView feedbackTextView;
    private DatabaseHelper dbHelper;
    public static int currentScore = 0;
    private int totalQuestions = 0;
    private List<Question> questions;
    private int currentQuestionIndex = 0;

    SQLiteDatabase db;
    DatabaseHelper dh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        dbHelper = new DatabaseHelper(getContext());

        questionTextView = view.findViewById(R.id.question_text_view);
        optionsRadioGroup = view.findViewById(R.id.options_radio_group);
        submitButton = view.findViewById(R.id.submit_button);
        scoreTextView = view.findViewById(R.id.score_text_view);
        feedbackTextView = view.findViewById(R.id.feedback_text_view);

        CardView questionCard = view.findViewById(R.id.question_card);
        questionCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_color));

        submitButton.setOnClickListener(v -> checkAnswer());

        // Load questions from the database
        loadQuestionsFromDatabase();
        Collections.shuffle(questions);

        loadNextQuestion();
        updateScoreDisplay();

        return view;
    }

    private void loadQuestionsFromDatabase() {
        List<String[]> flashcards = dbHelper.getAllFlashcards();
        questions = new ArrayList<>();

        for (String[] flashcard : flashcards) {
            String bisaya = flashcard[0];
            String english = flashcard[1];

            // Create a question for Bisaya to English
            Question questionBisayaToEnglish = new Question(
                    "What is the English translation of '" + bisaya + "'?",
                    generateOptions(flashcards, english),
                    english
            );
            questions.add(questionBisayaToEnglish);

            // Create a question for English to Bisaya
            Question questionEnglishToBisaya = new Question(
                    "What is the Bisaya translation of '" + english + "'?",
                    generateOptions(flashcards, bisaya),
                    bisaya
            );
            questions.add(questionEnglishToBisaya);
        }
    }

    private List<String> generateOptions(List<String[]> flashcards, String correctAnswer) {
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);

        List<String[]> shuffledFlashcards = new ArrayList<>(flashcards);
        Collections.shuffle(shuffledFlashcards);

        for (String[] flashcard : shuffledFlashcards) {
            if (options.size() < 4) {
                String option = flashcard[1];
                if (!option.equals(correctAnswer) && !options.contains(option)) {
                    options.add(option);
                }
            } else {
                break;
            }
        }

        Collections.shuffle(options);
        return options;
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex < 10) {
            Question question = questions.get(currentQuestionIndex);
            questionTextView.setText(question.getQuestionText());
            optionsRadioGroup.removeAllViews();

            for (String option : question.getOptions()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(option);
                radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.textbody));
                radioButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.option_background));
                radioButton.setPadding(24, 24, 24, 24);

                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 16);
                radioButton.setLayoutParams(params);

                optionsRadioGroup.addView(radioButton);
            }

            feedbackTextView.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            currentQuestionIndex++;
        } else {
            // Quiz finished
            questionTextView.setText("Quiz Completed!");
            optionsRadioGroup.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
            feedbackTextView.setVisibility(View.GONE);
        }
    }

    private void checkAnswer() {
        ContentValues values = new ContentValues();
        int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedOption = optionsRadioGroup.findViewById(selectedId);
            String answer = selectedOption.getText().toString();

            Question currentQuestion = questions.get(currentQuestionIndex - 1);
            totalQuestions++;
            boolean isCorrect = answer.equals(currentQuestion.getCorrectAnswer());

            if (isCorrect) {
                currentScore++;
                feedbackTextView.setText("Correct!");
                feedbackTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.correct_color));
                dh.incrementWordsLearned(1);
            } else {
                feedbackTextView.setText("Incorrect. The correct answer is: " + currentQuestion.getCorrectAnswer());
                feedbackTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.incorrect_color));
            }

            feedbackTextView.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);

            updateScoreDisplay();

            // Delay loading the next question
            optionsRadioGroup.postDelayed(this::loadNextQuestion, 4000);
        } else {
            Toast.makeText(getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateScoreDisplay() {
        scoreTextView.setText(String.format("Score: %d/%d", currentScore, totalQuestions));
    }

    // Question class to represent a single question
    private static class Question {
        private String questionText;
        private List<String> options;
        private String correctAnswer;

        public Question(String questionText, List<String> options, String correctAnswer) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestionText() {
            return questionText;
        }

        public List<String> getOptions() {
            return options;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }
    }
}

