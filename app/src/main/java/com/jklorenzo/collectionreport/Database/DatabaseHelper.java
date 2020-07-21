package com.jklorenzo.collectionreport.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CollectionReport.db";
    private String TABLE_NAME;
    public static final String[] NAMES = {"Edmund(Monthly)", "Edmund(Daily)", "Johnny(Monthly)", "Johnny(Daily)", "Ricky(Monthly)", "Charlie(Monthly)", "Romy(Monthly)", "Allan(Monthly)"};

    public DatabaseHelper(Context context, String TABLE_NAME) {
        super(context, DATABASE_NAME, null, 1);
        this.TABLE_NAME = TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + TABLE_NAME + " (NAME TEXT PRIMARY KEY");
        for (int j = 1; j <= 33; j++){
            sql.append(",DATA").append(j).append(" REAL");
        }
        sql.append(")");
        db.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StringBuilder sql = new StringBuilder(" (NAME TEXT PRIMARY KEY");
        for (int j = 1; j <= 33; j++){
            sql.append(",DATA").append(j).append(" REAL");
        }
        sql.append(")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + sql);
    }

    public void onChangeTable(String TABLE_NAME){
        SQLiteDatabase db = this.getWritableDatabase();
        this.TABLE_NAME = TABLE_NAME;
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (NAME TEXT PRIMARY KEY";
        for (int j = 1; j <= 33; j++){
            sql += ",DATA" + j + " REAL";
        }
        sql += ")";
        db.execSQL(sql);
    }

    public boolean updateData(String NAME, String Column, double Value){
        SQLiteDatabase db = this.getWritableDatabase();
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
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i <= 7; i++){
            ContentValues cv = new ContentValues();
            cv.put("NAME", NAMES[i]);
            for (int j = 1; j <= 33; j++){
                cv.put("DATA" + j, 0.0);
            }
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * from " + TABLE_NAME,null);
    }

    public Cursor getCollectorData(String NAME){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * FROM " + TABLE_NAME + " where NAME = '" + NAME + "'", null);
    }

    public long deleteData (String NAME) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "NAME = ?", new String[] {NAME});
    }

    public String getTABLE_NAME(){
        return TABLE_NAME;
    }
}
