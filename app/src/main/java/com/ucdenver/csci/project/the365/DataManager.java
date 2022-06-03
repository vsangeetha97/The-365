package com.ucdenver.csci.project.the365;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;


public class DataManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
        private static final String CREATE_EVENT_TABLE="create table  "+DBStructure.EVENT_TABLE_NAME +"(ID INTEGER primary key autoincrement, "
                + DBStructure.EVENT+" TEXT, "+ DBStructure.TIME+" TEXT, "+ DBStructure.DATE+" TEXT, "+ DBStructure.MONTH+" TEXT, "
                + DBStructure.YEAR+" TEXT, "+ DBStructure.Notify+" TEXT)";

        private static final String DROP_TABLE="DROP TABLE IF EXISTS "+DBStructure.EVENT_TABLE_NAME;

        public DataManager (@Nullable Context context)
        {

            super(context, DBStructure.DB_NAME, null, DBStructure.DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_EVENT_TABLE);
            }
            catch (SQLException e) {
                Log.i ("info", "In MySQLiteOpenHelper class onCreate method");
                Log.i ("info", e.getMessage());
            }



        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int DATABASE_VERSION, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
            }
            catch (SQLException e) {
                Log.i ("info", "In MySQLiteOpenHelper class onCreate method");
                Log.i ("info", e.getMessage());
            }

        }

        public void saveEvents(String event, String time, String date, String month,String year,String notify, SQLiteDatabase database) {

            ContentValues contentValues=new ContentValues();
            contentValues.put(DBStructure.EVENT, event);
            contentValues.put(DBStructure.TIME, time);
            contentValues.put(DBStructure.DATE, date);
            contentValues.put(DBStructure.MONTH, month);
            contentValues.put(DBStructure.YEAR, year);
            contentValues.put(DBStructure.Notify, notify);

            database.insert(DBStructure.EVENT_TABLE_NAME,null,contentValues);


        }

        public Cursor readEvents(String date, SQLiteDatabase db) {

            String[] Projections={DBStructure.EVENT, DBStructure.TIME,DBStructure.DATE,DBStructure.MONTH,DBStructure.YEAR};
            String selection= DBStructure.DATE +"=?";
            String[] selectionargs ={date};

            Cursor cursor=db.query(DBStructure.EVENT_TABLE_NAME,Projections,selection,selectionargs, null,null,null);
            return cursor;
        }

        public Cursor readIDEvents(String date,String event, String time, SQLiteDatabase db) {

            String[] Projections={DBStructure.ID, DBStructure.Notify};
            String selection= DBStructure.DATE +"=? and "+DBStructure.EVENT +"=? and "+DBStructure.TIME +"=?";
            String[] selectionargs ={date,event,time};

            Cursor cursor=db.query(DBStructure.EVENT_TABLE_NAME,Projections,selection,selectionargs, null,null,null);
            return cursor;
        }

        public Cursor readEventspermonth(String month,String year,SQLiteDatabase database) {

            String[] Projections={DBStructure.EVENT, DBStructure.TIME,DBStructure.DATE,DBStructure.MONTH,DBStructure.YEAR};
            String selection= DBStructure.MONTH +"=? and " + DBStructure.YEAR+"=? ";
            String[] selectionargs ={month,year};

            return  database.query(DBStructure.EVENT_TABLE_NAME,Projections,selection,selectionargs, null,null,null);
        }

        public void deleteEvent(String event, String date, String time, SQLiteDatabase database){
            String selection= DBStructure.EVENT+"=? and " + DBStructure.DATE+"=? and " + DBStructure.TIME+"=? ";
            String[] selectionargs ={event,date,time};

            database.delete(DBStructure.EVENT_TABLE_NAME,selection,selectionargs);
        }

        public void updateEvent(String date,String event, String time, String notify, SQLiteDatabase db){

            ContentValues contentValues=new ContentValues();
            contentValues.put(DBStructure.Notify, notify);

            String selection= DBStructure.DATE+"=? and " + DBStructure.EVENT+"=? and " + DBStructure.TIME+"=? ";
            String[] selectionargs ={date,event,time};


            db.update(DBStructure.EVENT_TABLE_NAME,contentValues,selection,selectionargs);

        }
}


