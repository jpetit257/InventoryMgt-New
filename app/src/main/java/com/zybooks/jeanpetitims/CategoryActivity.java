package com.zybooks.jeanpetitims;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryActivity extends AppCompatActivity
        implements CategoryDialogFragment.OnCategoryEnteredListener {

    private InventoryDatabase mInventoryDb;
    private CategoryAdapter mCategoryAdapter;
    private RecyclerView mRecyclerView;
    private int[] mCategoryColors;
    private Category mSelectedCategory;
    private int mSelectedCategoryPosition = RecyclerView.NO_POSITION;
    private ActionMode mActionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mCategoryColors = getResources().getIntArray(R.array.categoryColors);

        // Singleton
        mInventoryDb = InventoryDatabase.getInstance(getApplicationContext());

        mRecyclerView = findViewById(R.id.categoryRecyclerView);

        // Create 2 grid layout columns
        RecyclerView.LayoutManager gridLayoutManager =
                new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // Shows the available Categories
        mCategoryAdapter = new CategoryAdapter(loadCategoriesAsc());
        mRecyclerView.setAdapter(mCategoryAdapter);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu for the app bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Determine which app bar item was chosen
        switch (item.getItemId()) {
            case R.id.asc:
                mCategoryAdapter = new CategoryAdapter(loadCategoriesAsc());
                mRecyclerView.setAdapter(mCategoryAdapter);
                mInventoryDb.getCategories(InventoryDatabase.CategorySortOrder.UPDATE_ASC);
                return true;
            case R.id.desc:
                mCategoryAdapter = new CategoryAdapter(loadCategoriesDesc());
                mRecyclerView.setAdapter(mCategoryAdapter);
                mInventoryDb.getCategories(InventoryDatabase.CategorySortOrder.UPDATE_DESC);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private List<Category> loadCategoriesAsc() {
        return mInventoryDb.getCategories(InventoryDatabase.CategorySortOrder.UPDATE_ASC);
    }

    private List<Category> loadCategoriesDesc() {
        return mInventoryDb.getCategories(InventoryDatabase.CategorySortOrder.UPDATE_DESC);
    }

    @Override
    public void onCategoryEntered(String category) {
        // Returns category entered in the CategoryDialogFragment dialog
        if (category.length() > 0) {
            Category cat = new Category(category);
            if (mInventoryDb.addCategory(cat)) {
                mCategoryAdapter.addCategory(cat);
                Toast.makeText(this, "Added " + category, Toast.LENGTH_SHORT).show();
            } else {
                String message = getResources().getString(R.string.category_exists, category);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void addCategoryClick(View view) {
        // Prompt user to type new category
        FragmentManager manager = getSupportFragmentManager();
        CategoryDialogFragment dialog = new CategoryDialogFragment();
        dialog.show(manager, "CategoryDialog");
    }

    private class CategoryHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private Category mCategory;
        private TextView mTextView;

        public CategoryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recycler_view_items, parent, false));
            itemView.setOnClickListener(this);
            mTextView = itemView.findViewById(R.id.categoryTextView);
            itemView.setOnLongClickListener(this);
        }

        public void bind(Category category, int position) {
            mCategory = category;
            mTextView.setText(category.getName());

            if (mSelectedCategoryPosition == position) {
                // Make selected category stand out
                mTextView.setBackgroundColor(Color.RED);
            }
            else {
                // Make the background color dependent on the length of the category string
                int colorIndex = category.getName().length() % mCategoryColors.length;
                mTextView.setBackgroundColor(mCategoryColors[colorIndex]);
            }
        }

        @Override
        public void onClick(View view) {
            // Start ItemActivity, indicating what category was clicked
            Intent intent = new Intent(CategoryActivity.this, ItemActivity.class);
            intent.putExtra(ItemActivity.EXTRA_CATEGORY, mCategory.getName());
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            if (mActionMode != null) {
                return false;
            }

            mSelectedCategory = mCategory;
            mSelectedCategoryPosition = getAdapterPosition();

            // Re-bind the selected item
            mCategoryAdapter.notifyItemChanged(mSelectedCategoryPosition);

            // Show the CAB
            mActionMode = CategoryActivity.this.startActionMode(mActionModeCallback);

            return true;
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryHolder> {

        private List<Category> mCategoryList;

        public CategoryAdapter(List<Category> categories) {
            mCategoryList = categories;
        }

        @Override
        public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new CategoryHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, int position){
            holder.bind(mCategoryList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mCategoryList.size();
        }

        public void addCategory(Category category) {
            // Add the new category at the beginning of the list
            mCategoryList.add(0, category);

            // Notify the adapter that item was added to the beginning of the list
            notifyItemInserted(0);

            // Scroll to the top
            mRecyclerView.scrollToPosition(0);
        }

        public void removeCategory(Category category) {
            // Find category in the list
            int index = mCategoryList.indexOf(category);
            if (index >= 0) {
                // Remove the category
                mCategoryList.remove(index);

                // Notify adapter of category removal
                notifyItemRemoved(index);
            }
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Provide context menu for CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Process action item selection
            switch (item.getItemId()) {
                case R.id.delete:
                    // Delete from the database and remove from the RecyclerView
                    mInventoryDb.deleteCategory(mSelectedCategory);
                    mCategoryAdapter.removeCategory(mSelectedCategory);

                    // Close the CAB
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            // CAB closing, need to deselect item if not deleted
            mCategoryAdapter.notifyItemChanged(mSelectedCategoryPosition);
            mSelectedCategoryPosition = RecyclerView.NO_POSITION;
        }
    };
}