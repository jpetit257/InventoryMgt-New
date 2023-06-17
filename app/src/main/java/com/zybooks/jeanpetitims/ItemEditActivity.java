package com.zybooks.jeanpetitims;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ItemEditActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.zybooks.jeanpetitims.item_id";
    public static final String EXTRA_CATEGORY = "com.zybooks.jeanpetitims.category";

    private EditText mItemText;
    private EditText mDescriptionText;
    private EditText mQuantityText;

    private InventoryDatabase mInventoryDb;
    private long mItemId;
    private Item mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);

        mItemText = findViewById(R.id.itemText);
        mDescriptionText = findViewById(R.id.descriptionText);
        mQuantityText = findViewById(R.id.quantityText);

        mInventoryDb = InventoryDatabase.getInstance(getApplicationContext());

        // Get item ID from ItemActivity
        Intent intent = getIntent();
        mItemId = intent.getLongExtra(EXTRA_ITEM_ID, -1);



        if (mItemId == -1) {
            // Add new item
            mItem = new Item();
            setTitle(R.string.add_item);
        }
        else {
            // Update existing item
            mItem = mInventoryDb.getItem(mItemId);
            mItemText.setText(mItem.getName());
            mDescriptionText.setText(mItem.getDescription());
            mQuantityText.setText(mItem.getQty());
            setTitle(R.string.update_item);
        }

        String category = intent.getStringExtra(EXTRA_CATEGORY);
        mItem.setCategory(category);
    }

    public void saveButtonClick(View view) {

        mItem.setName(mItemText.getText().toString());
        mItem.setDescription(mDescriptionText.getText().toString());
        mItem.setQty(mQuantityText.getText().toString());

        if (!mItemText.getText().toString().equals("") &&
            !mQuantityText.getText().toString().equals(""))
        {
            if (mItemId == -1) {
                // New item
                mInventoryDb.addItem(mItem);
            } else {
                // Existing item
                mInventoryDb.updateItem(mItem);
            }

            // Send back item ID
            Intent intent = new Intent();
            intent.putExtra(EXTRA_ITEM_ID, mItem.getId());
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            Toast.makeText(this, "All fields are required. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}