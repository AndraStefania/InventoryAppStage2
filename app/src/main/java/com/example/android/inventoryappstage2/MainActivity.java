package com.example.android.inventoryappstage2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.Contract;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks <Cursor> {

    /**
     * Identifier for the inv product data loader
     */
    private static final int INV_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    InvCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView itListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        // There is no inv product data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InvCursorAdapter(this, null);
        itListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        itListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> adapterView, View view, int position, long id) {
                Toast.makeText(MainActivity.this, R.string.edit_product,
                        Toast.LENGTH_SHORT).show();

                // Create new intent to go to {@link AddEditActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific inv product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link InvEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.inventoryappstage2/inventoryappstage2/5"
                // if the inv product with ID 5 was clicked on.
                Uri currentItUri = ContentUris.withAppendedId(Contract.InvEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItUri);

                // Launch the {@link EditorActivity} to display the data for the selected inv product.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(INV_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/main_menu.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_data:
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader <Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                Contract.InvEntry._ID,
                Contract.InvEntry.COLUMN_NAME,
                Contract.InvEntry.COLUMN_PRICE,
                Contract.InvEntry.COLUMN_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                Contract.InvEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor data) {
        // Update {@link ItCursorAdapter} with this new cursor containing updated inv product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all entries in the database.
     */
    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(Contract.InvEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from inv database");
    }
}
