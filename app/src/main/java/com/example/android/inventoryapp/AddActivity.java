package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.android.inventoryapp.data.ProductProvider.LOG_TAG;


public class AddActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_CODE_PHOTO_GALLERY = 102;
    public static final String PHOTO_INTENT = "Choose photo";
    public static final String INTENT_TYPE_IMAGE_ANY = "image/*";
    private boolean mProductHasChanged = false;
    private String pictureString = "";

    @BindView(R.id.add_title_edit_text) EditText mTitleEditText;
    @BindView(R.id.add_quantity_edit_text) EditText mQuantityEditText;
    @BindView(R.id.add_price_edit_text) EditText mPriceEditText;
    @BindView(R.id.add_supplier_email_edit_text) EditText mSupportEmailEditText;
    @BindView(R.id.photoImageView) ImageView photoImageView;
    @BindView(R.id.galleryButton) Button galleryButton;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ButterKnife.bind(this);

        setGalleryButton();

        mTitleEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupportEmailEditText.setOnTouchListener(mTouchListener);

    }

    private void setGalleryButton() {
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent galleryIntent;
                if (Build.VERSION.SDK_INT < 19) {
                    galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                galleryIntent.setType(INTENT_TYPE_IMAGE_ANY);
                Intent photoChooser = Intent.createChooser(galleryIntent, PHOTO_INTENT);
                startActivityForResult(photoChooser, REQUEST_CODE_PHOTO_GALLERY);
            }
        });
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_PHOTO_GALLERY && resultCode == RESULT_OK) {
            showPhotoFromGallery(data);
        }
    }
    private void showPhotoFromGallery(final Intent data) {
        Uri photoImageUri = data.getData();
        pictureString = photoImageUri.toString();
        photoImageView.setImageBitmap(getBitmapFromUri(photoImageUri));
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        int targetW = photoImageView.getWidth();
        int targetH = photoImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    private void saveProduct(String titleString, int quantityInt, String pictureString, String emailString, int priceInt) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_TITLE, titleString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInt);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, pictureString);
        values.put(ProductEntry.COLUMN_PRODUCT_EMAIL, emailString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceInt);

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                String titleString = mTitleEditText.getText().toString().trim();
                Integer quantityInt;
                Integer priceInt;
                try {
                    quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());
                } catch (NumberFormatException e) {
                    quantityInt = 0;
                }
                try {
                    priceInt = Integer.parseInt(mPriceEditText.getText().toString());
                } catch (NumberFormatException e) {
                    priceInt = 0;
                }
                String emailString = mSupportEmailEditText.getText().toString().trim();

                if (titleString.equals("")) {
                    mTitleEditText.setError(getString(R.string.add_title_error));
                } else if (priceInt < 1) {
                    mPriceEditText.setError(getString(R.string.add_price_error));
                } else if (emailString.equals("")) {
                    mSupportEmailEditText.setError(getString(R.string.add_order_error));
                } else if (quantityInt < 1) {
                    mQuantityEditText.setError(getString(R.string.add_quantity_error));
                } else if (!pictureString.startsWith("content")) {
                    showEmptyTitleDialog();
                } else {
                    saveProduct(titleString, quantityInt, pictureString, emailString, priceInt);
                    finish();
                }
                return true;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    finish();
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showEmptyTitleDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        alertDialogBuilder.setView(inflater.inflate(R.layout.empty_view, null));
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton(android.R.string.ok , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.yes_msg, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
