package com.example.android.inventoryappstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by stefania.fanaru.
 */

public class InvProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InvProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inv table
     */
    private static final int INVS = 10;

    /**
     * URI matcher code for the content URI for a single inv product in the inv table
     */
    private static final int INV_ID = 11;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventoryappstage2/inventoryappstage2" will map to the
        // integer code {@link #INVS}. This URI is used to provide access to MULTIPLE rows
        // of the inv table.
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_INV, INVS);

        // The content URI of the form "content://com.example.android.inventoryappstage2/inventoryappstage2/#" will map to the
        // integer code {@link #INV_ID}. This URI is used to provide access to ONE single row
        // of the inv table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.inventoryappstage2/inventoryappstage2/5" matches, but
        // "content://com.example.android.inventoryappstage2/inventoryappstage2" (without a number at the end) doesn't match.
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_INV + "/#", INV_ID);
    }

    // Database helper object
    private InvDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InvDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                // For the INVS code, query the inv table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inv table.
                cursor = database.query(Contract.InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INV_ID:
                // For the INV_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryappstage2/inventoryappstage2/5",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 5 in this case.
                //
                // For every "?" in the selection, we need to have an inv product in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = Contract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the inv table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(Contract.InvEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                return insertInv(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an inv product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInv(Uri uri, ContentValues values) {

        // Sanity checking of the inserted attributes in ContentValues

        // Check that the name is not null
        String name = values.getAsString(Contract.InvEntry.COLUMN_NAME);
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer price = values.getAsInteger(Contract.InvEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Price field requires valid number");
        }
        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(Contract.InvEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity field requires valid number");
        }

        // Check that the supplier name is not null
        String sname = values.getAsString(Contract.InvEntry.COLUMN_SUPPLIER_NAME);
        if (sname == null) {
            throw new IllegalArgumentException("Supplier requires a name");
        }

        // Check that the supplier phone is not null and it contains 10 digits
        String phone = values.getAsString(Contract.InvEntry.COLUMN_SUPPLIER_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Supplier phone requires a phone number");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new inv products with the given values
        long id = database.insert(Contract.InvEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the inv device content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                return updateInv(uri, contentValues, selection, selectionArgs);
            case INV_ID:
                // For the INV_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = Contract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInv(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update inv product in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more inv products).
     * Return the number of rows that were successfully updated.
     */
    private int updateInv(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Sanity checking of the inserted attributes in ContentValues*/

        // If the name is provided, check that the name value is not null.
        if (values.containsKey(Contract.InvEntry.COLUMN_NAME)) {
            String name = values.getAsString(Contract.InvEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the price is provided, check that it's greater than or equal to 0
        if (values.containsKey(Contract.InvEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(Contract.InvEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Price field requires valid number");
            }
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(Contract.InvEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity field requires valid number");
        }

        // If the supplier name is provided, check that the supplier name value is not null.
        if (values.containsKey(Contract.InvEntry.COLUMN_SUPPLIER_NAME)) {
            String sname = values.getAsString(Contract.InvEntry.COLUMN_SUPPLIER_NAME);
            if (sname == null) {
                throw new IllegalArgumentException("Supplier requires a name");
            }
        }

        // If the supplier phone is provided, check that the supplier phone value is not null.
        if (values.containsKey(Contract.InvEntry.COLUMN_SUPPLIER_NAME)) {
            String phone = values.getAsString(Contract.InvEntry.COLUMN_SUPPLIER_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Supplier phone requires a phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(Contract.InvEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(Contract.InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INV_ID:
                // Delete a single row given by the ID in the URI
                selection = Contract.InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(Contract.InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                return Contract.InvEntry.CONTENT_LIST_TYPE;
            case INV_ID:
                return Contract.InvEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
