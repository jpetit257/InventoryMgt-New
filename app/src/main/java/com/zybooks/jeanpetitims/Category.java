package com.zybooks.jeanpetitims;

public class Category {

    private String mName;
    private long mUpdateTime;

    public Category() {}

    public Category(String name) {
        mName = name;
        mUpdateTime = System.currentTimeMillis();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }
}