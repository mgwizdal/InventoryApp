package com.example.android.inventoryapp;


import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;
import static android.content.ContentValues.TAG;
import static com.example.android.inventoryapp.data.ProductContract.*;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }




    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView  = (TextView) view.findViewById(R.id.main_title);
        TextView priceTextView  = (TextView) view.findViewById(R.id.main_price);
        TextView quantityTextView  = (TextView) view.findViewById(R.id.main_quantity);
        Button saleButton = (Button) view.findViewById(R.id.main_sale_button);

        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        String titleColumnIndex  = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_TITLE));
        double priceColumnIndex  = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));
        int quantityColumnIndex  = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));

        final int quantityFinal = quantityColumnIndex;

        titleTextView.setText(titleColumnIndex);
        priceTextView.setText(String.valueOf(priceColumnIndex));
        quantityTextView.setText(String.valueOf(quantityColumnIndex));
        final Context mContext = context;

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, productId);
                reduceQuantity(mContext, productUri, quantityFinal);
            }
        });
    }
    private void reduceQuantity(Context context, Uri productUri, int currentQuantity) {

        int oldQuantity = currentQuantity;
        int numRowsUpdated=0;
        if(oldQuantity > 0) {
            int newQuantity = oldQuantity - 1;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
            numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        }else {
            Toast.makeText(context, R.string.nothing_to_sale_msg ,Toast.LENGTH_SHORT).show();
        }
        if (!(numRowsUpdated > 0)) {
            Log.e(TAG, "Updating not working");
        }
    }

}
