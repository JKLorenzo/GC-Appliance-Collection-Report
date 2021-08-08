package com.jklorenzo.collectionreport.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CollectionReport.db";
    private SQLiteDatabase db;
    private String TABLE_NAME;
    public static final String[] NAMES = {"Edmund(Monthly)", "Edmund(Daily)", "Johnny(Monthly)", "Johnny(Daily)", "Ricky(Monthly)", "Romy(Monthly)", "Allan(Monthly)", "Delfin(Monthly)"};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        db = context.openOrCreateDatabase(DATABASE_NAME, context.MODE_PRIVATE, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        if (TABLE_NAME != null){
            openTable(TABLE_NAME);
        } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int month = calendar.get((Calendar.MONTH));
            int year = calendar.get(Calendar.YEAR);
            String[] monthText = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            openTable(monthText[month] + year);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void openTable(String TABLE_NAME){
        this.TABLE_NAME = TABLE_NAME;
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (NAME TEXT PRIMARY KEY";
        for (int j = 1; j <= 33; j++){
            sql += ",DATA" + j + " REAL";
        }
        sql += ")";
        db.execSQL(sql);
    }

    public boolean updateData(String NAME, String Column, double Value){
        ContentValues cv = new ContentValues();
        cv.put("NAME", NAME);
        cv.put(Column, Value);
        if (db.update(TABLE_NAME, cv, "NAME = ?", new String[]{NAME}) == 1){
            return true;
        } else {
            return false;
        }
    }

    public void setDefaultData(){
        for (int i = 0; i <= NAMES.length; i++){
            ContentValues cv = new ContentValues();
            cv.put("NAME", NAMES[i]);
            for (int j = 1; j <= 33; j++){
                cv.put("DATA" + j, 0.0);
            }
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public Cursor getAllData() {
        return db.rawQuery("SELECT * from " + TABLE_NAME,null);
    }

    public Cursor getCollectorData(String NAME){
        return db.rawQuery("select * FROM " + TABLE_NAME + " where NAME = '" + NAME + "'", null);
    }

    public long deleteData (String NAME) {
        return db.delete(TABLE_NAME, "NAME = ?", new String[] {NAME});
    }

    public String getTABLE_NAME(){
        return TABLE_NAME;
    }
}
