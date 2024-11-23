package com.agile.tuon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class QuizFragment extends Fragment {
    private TextView questionTextView;
    private RadioGroup optionsRadioGroup;
    private Button submitButton;
    private DatabaseHelper dbHelper;
    private int currentScore = 0;
    private int totalQuestions = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        dbHelper = new DatabaseHelper(getContext());

        questionTextView = view.findViewById(R.id.question_text_view);
        optionsRadioGroup = view.findViewById(R.id.options_radio_group);
        submitButton = view.findViewById(R.id.submit_button);

        submitButton.setOnClickListener(v -> checkAnswer());
        loadNextQuestion();

        return view;
    }

    private void loadNextQuestion() {
        // Implementation to load next question from database
        // This is a simplified version - you'll want to add proper question generation logic
        questionTextView.setText("What is 'salamat' in English?");
        optionsRadioGroup.removeAllViews();

        String[] options = {"thank you", "hello", "goodbye", "please"};
        for (String option : options) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(option);
            optionsRadioGroup.addView(radioButton);
        }
    }

    private void checkAnswer() {
        // Ensure that the optionsRadioGroup is not null
        if (optionsRadioGroup != null) {
            int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) { // Check if any radio button is selected
                RadioButton selectedOption = optionsRadioGroup.findViewById(selectedId);
                String answer = selectedOption.getText().toString();

                // Check if the answer is correct and update the score
                totalQuestions++;
                if (answer.equals("thank you")) {
                    currentScore++;
                }

                loadNextQuestion(); // Load the next question
            } else {
                // Optionally handle the case where no option is selected
                // e.g., show a Toast or a Snackbar
                Toast.makeText(getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
            }
        }
    }



}