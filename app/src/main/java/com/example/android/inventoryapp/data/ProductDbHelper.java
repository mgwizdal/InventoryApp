package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventoryapp.data.ProductContract.*;


public class ProductDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.COLUMN_PRODUCT_TITLE + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER, " +
                ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT, " +
                ProductEntry.COLUMN_PRODUCT_EMAIL + " TEXT, " +
                ProductEntry.COLUMN_PRODUCT_PRICE + " REAL );";
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}