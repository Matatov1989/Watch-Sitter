package com.sergeant_matatov.watchsitter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yurka on 29.08.2016.
 */
public class PersonSQL extends SQLiteOpenHelper {

    final String LOG_TAG = "myLogs";

    public PersonSQL(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "table ");
        String query;
        query = "create table if not exists Person(namePers text, phonePers text)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //добавить
    public void addNewPerson(PersonData pd)
    {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues cn = new ContentValues();
        cn.put("namePers", pd.getPerson());
        cn.put("phonePers", pd.getPhone());

        db.insert("Person", null, cn);
        db.close();
        Log.d(LOG_TAG, "add "+pd.getPerson().toString());
    }

    //вывести весь лист
    public List<PersonData> getAllPerson()
    {
        SQLiteDatabase db = getWritableDatabase();
        List<PersonData> listPD = new ArrayList<PersonData>();
        String query;
        query = "select * from Person";
        Cursor cr= db.rawQuery(query, null);
        if(cr.moveToFirst())
        {
            do
            {
                PersonData pd;
                String name;
                String phone;

                name = cr.getString(0);
                phone = cr.getString(1);
                pd = new PersonData(name, phone);
                listPD.add(pd);

            }while(cr.moveToNext());
        }
        return listPD;
    }

    //удалить
    public void delPerson(String pers)
    {
        SQLiteDatabase db = getWritableDatabase();
        String query = "delete from Person where namePers = '"+pers+"'";
        db.execSQL(query);
    }


}
