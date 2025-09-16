package com.zybooks.projecttwonickburnetteweightlossoption;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class WeightDatabaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 3;

    // Table Name and Columns
    private static final String TABLE_WEIGHT = "weights";

    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_WEIGHT = "weight";


    // initialize database, context to allow android access to db within app
    public WeightDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //C in crud
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WEIGHTS_TABLE = "CREATE TABLE IF NOT EXISTS weights ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "weight TEXT, "
                + "date TEXT)";
        db.execSQL(CREATE_WEIGHTS_TABLE);
    }

    // if need to change db structure, delete old table and create new table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS weights");
        onCreate(db);
    }

    // insert a new weight entry, U in crud
    public boolean insertWeight(String date, String weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_DATE, date);

        long result = db.insert(TABLE_WEIGHT, null, values);
        db.close();

        return result != -1; // Returns true if inserted successfully
    }

    // retrieve all weight entries, R in crud
    public ArrayList<WeightEntry> getAllWeights() {
        ArrayList<WeightEntry> weightList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT weight, date FROM weights ORDER BY id DESC" , null);

        if (cursor.moveToFirst()) {
            do {
                String weight = cursor.getString(1); //index should be 0,1 not 1,0 but this is giving correct result
                String date = cursor.getString(0);
                //Handle possible null values if date is missing in DB
                if (date == null) {
                    date = "Unknown Date"; //default value when missing date
                }
                weightList.add(new WeightEntry(weight, date)); //store both weight and date

            } while (cursor.moveToNext());

        }

        cursor.close();
        db.close();
        return weightList;
    }

    // delete last weight entries, D in crud
    public void deleteLastEntry() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM weights WHERE id = (SELECT MAX(id) FROM weights)");
        db.close();
    }
}
