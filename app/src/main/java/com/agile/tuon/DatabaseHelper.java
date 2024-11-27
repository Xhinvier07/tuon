package com.agile.tuon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TuonDB";
    private static final int DATABASE_VERSION = 2;

    // Tables
    public static final String TABLE_FLASHCARDS = "flashcards";
    public static final String TABLE_PROGRESS = "progress";
    public static final String TABLE_QUIZ_RESULTS = "quiz_results";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_BISAYA = "bisaya";
    public static final String COLUMN_ENGLISH = "english";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_DATE = "date";

    // Progress table columns
    public static final String COLUMN_WORDS_LEARNED = "words_learned";
    public static final String COLUMN_STREAK_DAYS = "streak_days";
    public static final String COLUMN_LAST_STUDY_DATE = "last_study_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create flashcards table
        String createFlashcardsTable = "CREATE TABLE " + TABLE_FLASHCARDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BISAYA + " TEXT,"
                + COLUMN_ENGLISH + " TEXT)";

        // Create progress table
        String createProgressTable = "CREATE TABLE " + TABLE_PROGRESS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_WORDS_LEARNED + " INTEGER,"
                + COLUMN_STREAK_DAYS + " INTEGER,"
                + COLUMN_LAST_STUDY_DATE + " TEXT)";

        // Create quiz results table
        String createQuizResultsTable = "CREATE TABLE " + TABLE_QUIZ_RESULTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SCORE + " INTEGER,"
                + COLUMN_DATE + " TEXT)";

        db.execSQL(createFlashcardsTable);
        db.execSQL(createProgressTable);
        db.execSQL(createQuizResultsTable);

        // Insert initial flashcard data
        insertInitialData(db);

        // Initialize progress
        initializeProgress(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        String[][] initialWords = {
                {"salamat", "thank you"},
                {"maayong buntag", "good morning"},
                {"maayong gabii", "good evening"},
                {"unsa", "what"},
                {"asa", "where"}
        };

        for (String[] word : initialWords) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BISAYA, word[0]);
            values.put(COLUMN_ENGLISH, word[1]);
            db.insert(TABLE_FLASHCARDS, null, values);
        }
    }

    private void initializeProgress(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORDS_LEARNED, 0);
        values.put(COLUMN_STREAK_DAYS, 0);
        values.put(COLUMN_LAST_STUDY_DATE, "");
        db.insert(TABLE_PROGRESS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_RESULTS);
        onCreate(db);
    }

    public void updateProgress(int wordsLearned, int streakDays, String lastStudyDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORDS_LEARNED, wordsLearned);
        values.put(COLUMN_STREAK_DAYS, streakDays);
        values.put(COLUMN_LAST_STUDY_DATE, lastStudyDate);

        db.update(TABLE_PROGRESS, values, COLUMN_ID + " = ?", new String[]{"1"});
    }

    public int[] getProgress() {
        SQLiteDatabase db = this.getReadableDatabase();
        int[] progress = new int[2]; // [wordsLearned, streakDays]

        Cursor cursor = db.query(TABLE_PROGRESS, new String[]{COLUMN_WORDS_LEARNED, COLUMN_STREAK_DAYS},
                COLUMN_ID + " = ?", new String[]{"1"}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            progress[0] = cursor.getInt(cursor.getColumnIndex(COLUMN_WORDS_LEARNED));
            progress[1] = cursor.getInt(cursor.getColumnIndex(COLUMN_STREAK_DAYS));
            cursor.close();
        }

        return progress;
    }

    public String getLastStudyDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastStudyDate = "";

        Cursor cursor = db.query(TABLE_PROGRESS, new String[]{COLUMN_LAST_STUDY_DATE},
                COLUMN_ID + " = ?", new String[]{"1"}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            lastStudyDate = cursor.getString(cursor.getColumnIndex(COLUMN_LAST_STUDY_DATE));
            cursor.close();
        }

        return lastStudyDate;
    }

    public void incrementWordsLearned(int increment) {
        int[] currentProgress = getProgress();
        int newWordsLearned = currentProgress[0] + increment;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORDS_LEARNED, newWordsLearned);

        db.update(TABLE_PROGRESS, values, COLUMN_ID + " = ?", new String[]{"1"});
    }

    public String[] getWordOfTheDay() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] wordOfTheDay = new String[2];

        Cursor cursor = db.query(TABLE_FLASHCARDS, new String[]{COLUMN_BISAYA, COLUMN_ENGLISH},
                null, null, null, null, "RANDOM()", "1");

        if (cursor != null && cursor.moveToFirst()) {
            wordOfTheDay[0] = cursor.getString(cursor.getColumnIndex(COLUMN_BISAYA));
            wordOfTheDay[1] = cursor.getString(cursor.getColumnIndex(COLUMN_ENGLISH));
            cursor.close();
        }

        return wordOfTheDay;
    }

    public List<String[]> getAllFlashcards() {
        List<String[]> flashcards = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FLASHCARDS, new String[]{COLUMN_BISAYA, COLUMN_ENGLISH},
                null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String[] wordPair = new String[2];
                wordPair[0] = cursor.getString(cursor.getColumnIndex(COLUMN_BISAYA));
                wordPair[1] = cursor.getString(cursor.getColumnIndex(COLUMN_ENGLISH));
                flashcards.add(wordPair);
            }
            cursor.close();
        }

        return flashcards;
    }
}
