package com.example.android.inventoryappstage2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by stefania.fanaru.
 */

public abstract class Contract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage2";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INV = "inventoryappstage2";

    /**
     * In order to prevent someone from accidentally instantiating the contract class,
     * we should create an empty constructor.
     */
    private Contract() {
    }

    /**
     * Inner class that defines constant values for the inv products database table.
     * Each entry in the table represents a single inv product.
     */

    public static abstract class InvEntry implements BaseColumns {
        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INV);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inv products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inv product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;

        // Name of the database table for products
        public static final String TABLE_NAME = "inv";

        /**
         * Unique ID number for the products (only for use in the database table).
         * Type: INTEGER
         */
        public final static String COLUMN_ID = "_id";

        /**
         * Name of the product.
         * Type: TEXT
         */
        public static final String COLUMN_NAME = "name";

        /**
         * Price of the product.
         * Type: REAL
         */
        public static final String COLUMN_PRICE = "price";

        /**
         * Quantity of the product.
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Name of the product supplier.
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Phone of the product supplier.
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

    }
}