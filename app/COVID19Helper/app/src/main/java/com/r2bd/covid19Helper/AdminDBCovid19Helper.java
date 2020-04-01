package com.r2bd.covid19Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminDBCovid19Helper extends SQLiteOpenHelper {

    public AdminDBCovid19Helper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table covid19_symptoms ( id int primary key, classification string, symptom string, checked int)");
        db.execSQL("create table covid19_whattodo ( id int primary key, classification string, description string, why string, checked int)");
        db.execSQL("create table covid19_advice ( id int primary key, description string, imagen blob)");
        /**
         * empty == 1 --> true is empty else  if empty == 0 --> false is not empty
         * date: last update
         */
        db.execSQL("create table covid19_state ( id int primary key, empty int, dateLastUpdate string)");
        //db.execSQL("insert into covid19_state (empty, dateLastUpdate) values (1, \"2020-03-28\")");
        db.execSQL("create table covid19_facts ( id int primary key, classification string, description string)");
        db.execSQL("create table covid19_howtostop ( id int primary key, description string, checked int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void update (String query){

    }
}
