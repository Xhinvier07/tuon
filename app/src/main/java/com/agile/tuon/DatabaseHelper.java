package com.agile.tuon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TuonDB";
    private static final int DATABASE_VERSION = 1;

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
                + COLUMN_BISAYA + " TEXT,"
                + "learned INTEGER,"
                + COLUMN_DATE + " TEXT)";

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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLASHCARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_RESULTS);
        onCreate(db);
    }

    public String[] getNextWord() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] wordPair = null;

        // Query to get a random row from the flashcards table
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_BISAYA + ", " + COLUMN_ENGLISH +
                " FROM " + TABLE_FLASHCARDS +
                " ORDER BY RANDOM() LIMIT 1", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                wordPair = new String[2];
                wordPair[0] = cursor.getString(cursor.getColumnIndex(COLUMN_BISAYA));
                wordPair[1] = cursor.getString(cursor.getColumnIndex(COLUMN_ENGLISH));
            }
            cursor.close();
        }
        return wordPair;
    }
}