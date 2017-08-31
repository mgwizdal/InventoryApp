package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    public static final String MAIL_INTENT_KEY = "mailto:";
    public static final String SUBJECT_INTENT_KEY = "?subject=";
    public static final String MESSAGE_INTENT_KEY = "&body=";

    private Uri mCurrentProductUri;
    double price;
    int quantity;
    private String title;
    private String email;
    private String pictureString = "";
    private Uri photoImageUri;

    @BindView(R.id.details_title) TextView mTitleTextView;
    @BindView(R.id.details_quantity) TextView mQuantityTextView;
    @BindView(R.id.details_price) TextView mPriceTextView;
    @BindView(R.id.details_order_button) Button mOrderButton;
    @BindView(R.id.details_quantity_minus) Button mQuantityMinusButton;
    @BindView(R.id.details_quantity_plus) Button mQuantityPlusButton;
    @BindView(R.id.details_image) ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mQuantityMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reduceQuantity(getApplicationContext(), mCurrentProductUri, quantity);
            }
        });
        mQuantityPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseQuantity(getApplicationContext(), mCurrentProductUri, quantity);
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = "Order of " + title;
                String message = "Hi, \n My new order is below: \nItem: " + title + "\nNumber of Pieces: \nThank you!\nKind regards,";
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                String uriText = MAIL_INTENT_KEY + Uri.encode(email) +
                        SUBJECT_INTENT_KEY + Uri.encode(subject) +
                        MESSAGE_INTENT_KEY + Uri.encode(message);
                Uri uri = Uri.parse(uriText);
                intent.setData(uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void increaseQuantity(Context context, Uri productUri, int currentQuantity) {
        int oldQuantity = currentQuantity;
        int numRowsUpdated = 0;
        int newQuantity = oldQuantity + 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
        numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (!(numRowsUpdated > 0)) {
            Log.e("Details", "inserting not working");
        }
    }

    private void reduceQuantity(Context context, Uri productUri, int currentQuantity) {
        int oldQuantity = currentQuantity;
        int numRowsUpdated = 0;
        if (oldQuantity > 0) {
            int newQuantity = oldQuantity - 1;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
            numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        } else {
            Toast.makeText(context, R.string.nothing_to_sale_msg, Toast.LENGTH_SHORT).show();
        }
        if (!(numRowsUpdated > 0)) {
            Log.e("Details", "deleting not working");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_TITLE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_EMAIL,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_TITLE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

            title = cursor.getString(titleColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            pictureString = cursor.getString(pictureColumnIndex);
            email = cursor.getString(emailColumnIndex);
            price = cursor.getDouble(priceColumnIndex);

            String quantityString = String.valueOf(quantity);
            String priceString = String.valueOf(price);
            if (!pictureString.equals("")) {
                setImage();
            }
            mTitleTextView.setText(title);
            mQuantityTextView.setText(quantityString);
            mPriceTextView.setText(priceString);
        }
    }

    public void setImage() {
        photoImageUri = null;
        photoImageUri = Uri.parse(pictureString);
        mImageView.setImageURI(photoImageUri);
        mImageView.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
        quantity = 0;
        price = 0;
        mImageView.setImageURI(null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.details_delete_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.details_delete_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}