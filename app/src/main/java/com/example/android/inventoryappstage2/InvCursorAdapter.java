package com.example.android.inventoryappstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.Contract;

import static com.example.android.inventoryappstage2.data.Contract.InvEntry.COLUMN_NAME;
import static com.example.android.inventoryappstage2.data.Contract.InvEntry.COLUMN_PRICE;

/**
 * Created by stefania.fanaru.
 */

public class InvCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InvCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inv inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current inv product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.inv_name);
        TextView priceTextView = view.findViewById(R.id.inv_price);
        TextView quantityTextView = view.findViewById(R.id.inv_quantity);

        Button saleButton = view.findViewById(R.id.sale);

        // Find the columns of inv product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(COLUMN_NAME);
        int priceColumnIndex = cursor.getColumnIndex(COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(Contract.InvEntry.COLUMN_QUANTITY);

        // Read the inv product attributes from the Cursor for the current inv product
        final String invName = cursor.getString(nameColumnIndex);
        final String invPrice = cursor.getString(priceColumnIndex);
        String invQuantity = cursor.getString(quantityColumnIndex);

        // Update the TextViews with the attributes for the current inv product
        nameTextView.setText(invName);
        priceTextView.setText(invPrice);
        quantityTextView.setText(invQuantity);

        final int idColumnIndex = cursor.getInt(cursor.getColumnIndex(Contract.InvEntry._ID));
        final int currentQuantityColumnIndex = cursor.getColumnIndex(Contract.InvEntry.COLUMN_QUANTITY);
        final int currentQuantity = Integer.valueOf(cursor.getString(currentQuantityColumnIndex));

        //Sell button which decrease quantity in storage
        saleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentQuantity > 0) {
                    int newCurrentQuantity = currentQuantity - 1;
                    Uri quantityUri = ContentUris.withAppendedId(Contract.InvEntry.CONTENT_URI, idColumnIndex);

                    ContentValues values = new ContentValues();
                    values.put(Contract.InvEntry.COLUMN_QUANTITY, newCurrentQuantity);
                    context.getContentResolver().update(quantityUri, values, null, null);

                    Toast.makeText(context, "The sale was successfully! \nThe new quantity for " + invName + " is: " + newCurrentQuantity, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "You can't sale, because " + invName + " is out of stock!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

