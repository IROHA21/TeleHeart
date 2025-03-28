package com.moon.TeleHeart.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "messages.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CONTENT = "content";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CONTENT + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Save messages to the database
    public void saveMessages(List<String> messages) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String message : messages) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CONTENT, message);
                db.insert(TABLE_MESSAGES, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    // Retrieve messages from the database
    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);

        int contentColumnIndex = cursor.getColumnIndex(COLUMN_CONTENT);
        if (contentColumnIndex >= 0) { // Ensure the column exists
            if (cursor.moveToFirst()) {
                do {
                    String content = cursor.getString(contentColumnIndex);
                    messages.add(content);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return messages;
    }

    // Clear all messages from the database
    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MESSAGES);
        db.close();
    }

}
