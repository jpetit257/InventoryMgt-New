package com.zybooks.jeanpetitims;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ItemActivity extends AppCompatActivity {

    private final int REQUEST_CODE_NEW_ITEM = 0;
    private final int REQUEST_CODE_UPDATE_ITEM = 1;

    public static final String EXTRA_CATEGORY = "com.zybooks.jeanpetitims.category";

    private InventoryDatabase mInventoryDb;
    private String mCategory;
    private List<Item> mItemList;
    private TextView mItemText;
    private TextView mDescriptionLabel;
    private TextView mDescriptionText;
    private TextView mQuantityLabel;
    private TextView mQuantityText;
    private int mCurrentItemIndex;
    private ViewGroup mShowItemsLayout;
    private ViewGroup mNoItemsLayout;
    private Item mDeletedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        // Hosting activity provides the category of the items to display
        Intent intent = getIntent();
        mCategory = intent.getStringExtra(EXTRA_CATEGORY);

        // Load all items for this category
        mInventoryDb = InventoryDatabase.getInstance(getApplicationContext());
        mItemList = mInventoryDb.getItems(mCategory);

        mItemText = findViewById(R.id.itemText);
        mDescriptionLabel = findViewById(R.id.descriptionLabel);
        mDescriptionText = findViewById(R.id.descriptionText);
        mQuantityLabel = findViewById(R.id.quantityLabel);
        mQuantityText = findViewById(R.id.quantityText);
        mShowItemsLayout = findViewById(R.id.showItemsLayout);
        mNoItemsLayout = findViewById(R.id.noItemsLayout);

        // Show first item
        showItem(0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Are there items to display?
        if (mItemList.size() == 0) {
            updateAppBarTitle();
            displayItem(false);
        } else {
            displayItem(true);
            toggleDescriptionVisibility();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu for the app bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Determine which app bar item was chosen
        switch (item.getItemId()) {
            case R.id.previous:
                showItem(mCurrentItemIndex - 1);
                return true;
            case R.id.next:
                showItem(mCurrentItemIndex + 1);
                return true;
            case R.id.add:
                addItem();
                return true;
            case R.id.edit:
                editItem();
                return true;
            case R.id.delete:
                deleteItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addItemButtonClick(View view) {
        addItem();
    }

    public void answerButtonClick(View view) {
        toggleDescriptionVisibility();
    }

    private void displayItem(boolean display) {

        // Show or hide the appropriate screen
        if (display) {
            mShowItemsLayout.setVisibility(View.VISIBLE);
            mNoItemsLayout.setVisibility(View.GONE);
        } else {
            mShowItemsLayout.setVisibility(View.GONE);
            mNoItemsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateAppBarTitle() {

        // Display category and number of items in app bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = getResources().getString(R.string.item_number,
                    mCategory, mCurrentItemIndex + 1, mItemList.size());
            setTitle(title);
        }
    }

    private void addItem() {
        Intent intent = new Intent(this, ItemEditActivity.class);
        intent.putExtra(ItemEditActivity.EXTRA_CATEGORY, mCategory);
        startActivityForResult(intent, REQUEST_CODE_NEW_ITEM);
    }

    private void editItem() {
        if (mCurrentItemIndex >= 0) {
            Intent intent = new Intent(this, ItemEditActivity.class);
            intent.putExtra(EXTRA_CATEGORY, mCategory);
            long itemId = mItemList.get(mCurrentItemIndex).getId();
            intent.putExtra(ItemEditActivity.EXTRA_ITEM_ID, itemId);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_ITEM);
        }
    }

    private void deleteItem() {
        if (mCurrentItemIndex >= 0) {
            // Save item in case user undoes delete
            mDeletedItem = mItemList.get(mCurrentItemIndex);

            mInventoryDb.deleteItem(mDeletedItem.getId());
            mItemList.remove(mCurrentItemIndex);

            if (mItemList.size() == 0) {
                // No items left to show
                mCurrentItemIndex = -1;
                updateAppBarTitle();
                displayItem(false);
            }
            else {
                showItem(mCurrentItemIndex);
            }

            // Show delete message with Undo button
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                    R.string.item_deleted, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add item back
                    mInventoryDb.addItem(mDeletedItem);
                    mItemList.add(mDeletedItem);
                    showItem(mItemList.size() - 1);
                    displayItem(true);
                }
            });
            snackbar.show();
        }
    }

    private void showItem(int itemIndex) {

        // Show item at the given index
        if (mItemList.size() > 0) {
            if (itemIndex < 0) {
                itemIndex = mItemList.size() - 1;
            } else if (itemIndex >= mItemList.size()) {
                itemIndex = 0;
            }

            mCurrentItemIndex = itemIndex;
            updateAppBarTitle();

            Item item = mItemList.get(mCurrentItemIndex);
            mItemText.setText(item.getName());
            mDescriptionText.setText(item.getDescription());
            mQuantityText.setText(item.getQty());
        }
        else {
            // No items yet
            mCurrentItemIndex = -1;
        }
    }

    private void toggleDescriptionVisibility() {
        if (mQuantityText.getVisibility() == View.VISIBLE) {
            //mAnswerButton.setText(R.string.show_answer);
            mQuantityText.setVisibility(View.VISIBLE);
            mQuantityLabel.setVisibility(View.VISIBLE);
        }
        else {
            //mAnswerButton.setText(R.string.hide_answer);
            mQuantityText.setVisibility(View.VISIBLE);
            mQuantityLabel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_NEW_ITEM) {
            // Get added item
            long itemId = data.getLongExtra(ItemEditActivity.EXTRA_ITEM_ID, -1);
            Item newItem = mInventoryDb.getItem(itemId);

            // Add newly created item to the item list and show it
            mItemList.add(newItem);
            showItem(mItemList.size() - 1);

            Toast.makeText(this, R.string.item_added, Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_ITEM) {
            // Get updated item
            long itemId = data.getLongExtra(ItemEditActivity.EXTRA_ITEM_ID, -1);
            Item updatedItem = mInventoryDb.getItem(itemId);

            // Replace current item in item list with updated item
            Item currentItem = mItemList.get(mCurrentItemIndex);
            currentItem.setName(updatedItem.getName());
            currentItem.setDescription(updatedItem.getDescription());
            currentItem.setQty(updatedItem.getQty());
            showItem(mCurrentItemIndex);

            Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
        }
    }
}