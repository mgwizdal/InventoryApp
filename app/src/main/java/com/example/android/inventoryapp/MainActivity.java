package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.list) ListView placeListView;
    @BindView(R.id.empty_view) View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Timber.plant(new Timber.DebugTree());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, com.example.android.inventoryapp.AddActivity.class);
                startActivity(intent);
            }
        });

        placeListView.setEmptyView(emptyView);
        mCursorAdapter = new ProductCursorAdapter(this,null);
        placeListView.setAdapter(mCursorAdapter);

        placeListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                Uri currentPlaceUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentPlaceUri);
                startActivity(intent);
            }
        });
        placeListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                Uri currentPlaceUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentPlaceUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(PRODUCT_LOADER , null, this);
    }

    private void insertDummyData() {
        String productTitle = getString(R.string.dummy_title);
        int productQuantity = 5;
        double productPrice = 10.99;
        String productPicture = getString(R.string.dummy_picture_string_uri);
        String productEmail = getString(R.string.dummy_email);
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_TITLE, productTitle);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, productPicture + R.drawable.obraz);
        values.put(ProductEntry.COLUMN_PRODUCT_EMAIL, productEmail);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    private void deleteAllPlace() {
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_msg);
        builder.setPositiveButton(R.string.no_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.discard_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                    int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
                    Timber.i(rowsDeleted + " rows deleted from products database");
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllPlace();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_TITLE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
        };
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}