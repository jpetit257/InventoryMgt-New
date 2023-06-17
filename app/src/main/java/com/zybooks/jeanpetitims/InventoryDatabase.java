package com.zybooks.jeanpetitims;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import org.mindrot.BCrypt;

import java.util.ArrayList;
import java.util.List;




public class InventoryDatabase extends SQLiteOpenHelper {

    // Database version and name
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "inventory.db";

    private static InventoryDatabase mInventoryDb;

    public enum CategorySortOrder { UPDATE_DESC, UPDATE_ASC }

    // Singleton pattern to ensure only one instance of the DB exists
    public static InventoryDatabase getInstance(Context context) {
        if (mInventoryDb == null) {
            mInventoryDb = new InventoryDatabase(context);
        }
        return mInventoryDb;
    }

    private InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Table and column names for the 'users' table
    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
    }

    // Table and column names for the 'categories' table
    private static final class CategoryTable {
        private static final String TABLE = "categories";
        private static final String COL_NAME = "name";
        private static final String COL_UPDATE_TIME = "updated";
    }

    // Table and column names for the 'items' table
    private static final class ItemTable {
        private static final String TABLE = "items";
        private static final String COL_ID = "_id";
        private static final String COL_NAME = "name";
        private static final String COL_DESCRIPTION = "description";
        private static final String COL_QTY = "qty";
        private static final String COL_CATEGORY = "category";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create users table
        db.execSQL("CREATE TABLE " + UserTable.TABLE + " (" +
                UserTable.COL_USERNAME + " primary key, " +
                UserTable.COL_PASSWORD + " Text )");

        // Create categories table
        db.execSQL("CREATE TABLE " + CategoryTable.TABLE + " (" +
                CategoryTable.COL_NAME + " primary key, " +
                CategoryTable.COL_UPDATE_TIME + " int)");

        // Create items table with foreign key that cascade deletes
        db.execSQL("CREATE TABLE " + ItemTable.TABLE + " (" +
                ItemTable.COL_ID + " integer primary key autoincrement, " +
                ItemTable.COL_NAME + ", " +
                ItemTable.COL_DESCRIPTION + ", " +
                ItemTable.COL_QTY + ", " +
                ItemTable.COL_CATEGORY + ", " +
                "foreign key(" + ItemTable.COL_CATEGORY + ") references " +
                CategoryTable.TABLE + "(" + CategoryTable.COL_NAME + ") on delete cascade)");

        // Add some categories
        String[] categories = { "Appliances", "Computers", "Electronics", "HomeKitchen" };
        for (String cat: categories) {
            Category category = new Category(cat);
            ContentValues values = new ContentValues();
            values.put(CategoryTable.COL_NAME, category.getName());
            values.put(CategoryTable.COL_UPDATE_TIME, category.getUpdateTime());
            db.insert(CategoryTable.TABLE, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables and recreate them on database upgrade
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ItemTable.TABLE);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                db.execSQL("pragma foreign_keys = on;");
            } else {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }
    }

    // Retrieve a list of categories from the 'categories' table, ordered based on the given sort order
    public List<Category> getCategories(CategorySortOrder order) {
        List<Category> categories = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String orderBy;
        switch (order) {
            case UPDATE_ASC:
                orderBy = CategoryTable.COL_NAME + " asc";
                break;
            case UPDATE_DESC:
                orderBy = CategoryTable.COL_NAME + " desc";
                break;
            default:
                orderBy = CategoryTable.COL_NAME + " collate nocase";
                break;
        }

        String sql = "SELECT * FROM " + CategoryTable.TABLE + " ORDER BY " + orderBy;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setName(cursor.getString(0));
                category.setUpdateTime(cursor.getLong(1));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return categories;
    }

    // Check if there are existing users in the 'users' table
    public boolean isUsersExist() {
        SQLiteDatabase db = this.getWritableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + UserTable.TABLE + ";", null)) {
            return cursor.getCount() > 0;
        }
    }

    // Add a new user to the 'users' table with the given username and password
    public boolean addUser(String user, String pass) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, user);
        String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
        values.put(UserTable.COL_PASSWORD, hashedPassword);
        long itemId = db.insert(UserTable.TABLE, null, values);
        return itemId != -1;
    }

    // Check if a user with the given username exists in the 'users' table
    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + UserTable.TABLE + " WHERE Username = ?", new String[]{username})) {
            return cursor.getCount() > 0;
        }
    }

    // Check if a user with the given username and password exists in the 'users' table
    public boolean checkUserAndPassword(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + UserTable.TABLE + " WHERE username = ?", new String[] {username});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int passwordColumnIndex = cursor.getColumnIndex(UserTable.COL_PASSWORD);
            if (passwordColumnIndex != -1) {
                String hashedPassword = cursor.getString(passwordColumnIndex);
                return BCrypt.checkpw(password, hashedPassword);
            }
        }

        // Close connection
        cursor.close();

        // Access Denied
        return false;
    }

    // Add a new category to the 'categories' table
    public boolean addCategory(Category category) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CategoryTable.COL_NAME, category.getName());
        values.put(CategoryTable.COL_UPDATE_TIME, category.getUpdateTime());
        long id = db.insert(CategoryTable.TABLE, null, values);
        return id != -1;
    }

    // Update the details of a category in the 'categories' table
    public void updateCategory(Category category) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CategoryTable.COL_NAME, category.getName());
        values.put(CategoryTable.COL_UPDATE_TIME, category.getUpdateTime());
        db.update(CategoryTable.TABLE, values,
                CategoryTable.COL_NAME + " = ?", new String[] { category.getName() });
    }

    // Delete a category from the 'categories' table
    public void deleteCategory(Category category) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(CategoryTable.TABLE,
                CategoryTable.COL_NAME + " = ?", new String[] { category.getName() });
    }

    // Retrieve a list of items belonging to a specific category from the 'items' table
    public List<Item> getItems(String category) {
        List<Item> items = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + ItemTable.TABLE +
                " WHERE " + ItemTable.COL_CATEGORY + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { category });
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.setId(cursor.getInt(0));
                item.setName(cursor.getString(1));
                item.setDescription(cursor.getString(2));
                item.setQty(cursor.getString(3));
                item.setCategory(cursor.getString(4));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return items;
    }

    // Retrieve a single item with the given itemId from the 'items' table
    public Item getItem(long itemId) {
        Item item = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + ItemTable.TABLE +
                " WHERE " + ItemTable.COL_ID + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { Float.toString(itemId) });

        if (cursor.moveToFirst()) {
            item = new Item();
            item.setId(cursor.getInt(0));
            item.setName(cursor.getString(1));
            item.setDescription(cursor.getString(2));
            item.setQty(cursor.getString(3));
            item.setCategory(cursor.getString(4));
        }

        // Close connection
        cursor.close();

        return item;
    }

    // Add a new item to the 'items' table
    public void addItem(Item item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemTable.COL_NAME, item.getName());
        values.put(ItemTable.COL_DESCRIPTION, item.getDescription());
        values.put(ItemTable.COL_QTY, item.getQty());
        values.put(ItemTable.COL_CATEGORY, item.getCategory());
        long itemId = db.insert(ItemTable.TABLE, null, values);
        item.setId(itemId);

        // Change update time in categories table
        updateCategory(new Category(item.getCategory()));

        // Send SMS if inventory is low
    }

    // Update the details of an item in the 'items' table
    public void updateItem(Item item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemTable.COL_ID, item.getId());
        values.put(ItemTable.COL_NAME, item.getName());
        values.put(ItemTable.COL_DESCRIPTION, item.getDescription());
        values.put(ItemTable.COL_QTY, item.getQty());
        values.put(ItemTable.COL_CATEGORY, item.getCategory());
        db.update(ItemTable.TABLE, values,
                ItemTable.COL_ID + " = " + item.getId(), null);

        // Change update time in categories table
        updateCategory(new Category(item.getCategory()));
    }

    // Delete an item from the 'items' table
    public void deleteItem(long itemId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ItemTable.TABLE,
                ItemTable.COL_ID + " = " + itemId, null);
    }
}