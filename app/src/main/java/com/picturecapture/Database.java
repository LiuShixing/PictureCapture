package com.picturecapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 明白 on 2016/5/10.
 */
public class Database extends SQLiteOpenHelper{
    private final static String DB_NAME = "IAMGE_DB";
    private final static String TABLE_NAME = "PICTURE_LIB";
    private final static String ID = "ID";
    private final static String DATE = "DATE";
    private final static String IMAGE = "IMAGE";
    private final static int VERSION = 1;

    private File mDbFile;
    private SQLiteDatabase mDB;

    private final static Object locker = new Object();
    public Database(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("e","create table"+TABLE_NAME);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void init()
    {
        boolean isSdExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(isSdExist)
        {
             File sdPath = Environment.getExternalStorageDirectory();
             String dict_file = sdPath.getPath()+"/Mingbai";
             File dictFile = new File(dict_file);
             mDbFile = new File(dict_file+"/"+DB_NAME);

            if(!dictFile.exists())
            {
                dictFile.mkdir();
            }
            if(!mDbFile.exists())
            {
                try {
                    mDbFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mDB = SQLiteDatabase.openOrCreateDatabase(mDbFile, null);
            String sql = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+DATE+" INTEGER,"+IMAGE+" BLOB);";
            mDB.execSQL(sql);
        }

    }

    public long insertBitmaps(byte[] bytes)
    {
        synchronized (locker) {
            mDB = SQLiteDatabase.openOrCreateDatabase(mDbFile, null);
            ContentValues contentValues = new ContentValues();
            java.util.Date d = new Date();
            long dateTime = d.getTime();
            contentValues.put(DATE, dateTime);
            contentValues.put(IMAGE, bytes);
            return mDB.insert(TABLE_NAME, null, contentValues);
        }
    }
    public void deleteImage(long id)
    {
        mDB = SQLiteDatabase.openOrCreateDatabase(mDbFile,null);
        mDB.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+ID+" = "+id);
    }

    public ArrayList<Long> queryImageIds()
    {
        mDB = SQLiteDatabase.openOrCreateDatabase(mDbFile,null);
        Cursor cursor = mDB.rawQuery("SELECT "+ID+" FROM "+TABLE_NAME+" ORDER BY "+DATE+" DESC",null);
        ArrayList<Long> ids = new ArrayList<>();

        Log.e("e","cursor.getCount()="+cursor.getCount());
        while(cursor.moveToNext())
        {
            ids.add(cursor.getLong(cursor.getColumnIndex(ID)));
        }

        return ids;
    }
    public ArrayList<Byte> queryImage(long id) {

        Log.e("e","id="+id);
        mDB = SQLiteDatabase.openOrCreateDatabase(mDbFile, null);
        Cursor cursor = mDB.rawQuery("SELECT " + IMAGE + " FROM " + TABLE_NAME + " WHERE " + ID + " = " + id, null);
        ArrayList<Byte> bytesList = new ArrayList<>();
        while(cursor.moveToNext()) {
            byte[] bytes = cursor.getBlob(0);
            for (byte b:bytes) {
                bytesList.add(b);
            }
            break;
        }
        return bytesList;
    }

}
