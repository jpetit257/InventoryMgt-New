package com.zybooks.jeanpetitims;

public class Item {

    private long mId;
    private String mName;
    private String mDescription;
    private String mQty;
    private String mCategory;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public String getQty() {
        return mQty;
    }

    public void setQty(String qty) {
        this.mQty = qty;
    }
}