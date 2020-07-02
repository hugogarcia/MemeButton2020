package com.garciaapps.botoesdememe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by HUGO on 13/06/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "favoritos";
    private static final String COL1 = "ID";
    private static final String COL2 = "imagem";
    private static final String COL3 = "som";
    private static final String COL4 = "tipo";
    private static final String COL5 = "descricao";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE "+ TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, "+
                COL3 + " TEXT, "+
                COL4 + " TEXT, "+
                COL5 + " TEXT) ";

        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(String imagem, String som, String tipo, String descricao){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, imagem);
        contentValues.put(COL3, som);
        contentValues.put(COL4, tipo);
        contentValues.put(COL5, descricao);

        long result = db.insert(TABLE_NAME, null, contentValues);
        //retorna -1 se der errado
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getData(String tipo){
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor data = database.query(TABLE_NAME,
                null,
                COL4+ " = ?",
                new String[]{tipo},
                null,
                null,
                null);

        return data;
    }

    public boolean checarAudio(String som){
        SQLiteDatabase database = this.getWritableDatabase();

        String Query = "Select * from " + TABLE_NAME + " where " + COL3 + " = " + "'" + som + "'";
        Cursor cursor = database.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void delete(String som){
        SQLiteDatabase database = this.getWritableDatabase();
        String selection = COL3 + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { som };
        // Issue SQL statement.
        database.delete(TABLE_NAME, selection, selectionArgs);
    }
}
