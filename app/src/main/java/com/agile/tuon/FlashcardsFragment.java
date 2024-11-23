package com.agile.tuon;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class FlashcardsFragment extends Fragment {
    private TextView wordTextView;
    private TextView translationTextView;
    private CardView flashcard;
    private ArrayList<String[]> wordList; // Store word pairs
    private int currentIndex = 0; // Current word index
    private boolean isShowingTranslation = false; // Track which side is shown
    private TextToSpeech textToSpeech;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcards, container, false);

        textToSpeech = ((MainActivity) getActivity()).getTextToSpeech();
        wordTextView = view.findViewById(R.id.word_text_view);
        translationTextView = view.findViewById(R.id.translation_text_view);
        flashcard = view.findViewById(R.id.flashcard);
        Button pronounceButton = view.findViewById(R.id.pronounce_button);
        Button nextButton = view.findViewById(R.id.next_button);
        Button previousButton = view.findViewById(R.id.previous_button);

        // Initialize word list
        wordList = new ArrayList<>();
        loadWords(); // Load words from the database or a predefined list

        flashcard.setOnClickListener(v -> flipCard());
        pronounceButton.setOnClickListener(v -> pronounceWord());
        nextButton.setOnClickListener(v -> loadNextWord());
        previousButton.setOnClickListener(v -> loadPreviousWord());

        loadCurrentWord(); // Load the first word
        return view;
    }

    private void flipCard() {
        // Flip animation logic
        final Animation flipAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.flip);
        flashcard.startAnimation(flipAnimation);

        flipAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Toggle visibility based on current state
                if (isShowingTranslation) {
                    wordTextView.setVisibility(View.VISIBLE);
                    translationTextView.setVisibility(View.GONE);
                } else {
                    wordTextView.setVisibility(View.GONE);
                    translationTextView.setVisibility(View.VISIBLE);
                }
                isShowingTranslation = !isShowingTranslation; // Toggle state
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void pronounceWord() {
        String wordToPronounce = isShowingTranslation ? translationTextView.getText().toString() : wordTextView.getText().toString();
        textToSpeech.speak(wordToPronounce, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void loadNextWord() {
        if (currentIndex < wordList.size() - 1) {
            currentIndex++;
            loadCurrentWord();
        }
    }

    private void loadPreviousWord() {
        if (currentIndex > 0) {
            currentIndex--;
            loadCurrentWord();
        }
    }

    private void loadCurrentWord() {
        String[] wordPair = wordList.get(currentIndex);
        String word = wordPair[0]; // The Bisaya word
        String translation = wordPair[1]; // The English translation

        wordTextView.setText(word);
        translationTextView.setText(translation);
        translationTextView.setVisibility(View.GONE); // Hide translation initially
        wordTextView.setVisibility(View.VISIBLE); // Show Bisaya word
        isShowingTranslation = false; // Reset flip state
    }

    private void loadWords() {
        // Load words from the database or a predefined list
        // Example: Adding word pairs (Bisaya, English)
        wordList.add(new String[]{"Kumusta", "Hello"});
        wordList.add(new String[]{"Salamat", "Thank you"});
        wordList.add(new String[]{"Palihug", "Please"});
        // Add more words as needed
    }
}