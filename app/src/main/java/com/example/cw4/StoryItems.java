package com.example.cw4;

import android.graphics.Bitmap;

public class StoryItems {

    private Bitmap imageResource;
    private String tagStory,DateStory;
    private Boolean isItemChecked;

    public StoryItems(Bitmap imageResource, String tagStory, String dateStory, Boolean isItemChecked) {
        this.imageResource = imageResource;
        this.tagStory = tagStory;
        this.DateStory = dateStory;
        this.isItemChecked = isItemChecked;
    }



    public Bitmap getImageResource() {
        return imageResource;
    }

    public void setImageResource(Bitmap imageResource) {
        this.imageResource = imageResource;
    }

    public String getTag() {
        return tagStory;
    }

    public void setTag(String tagStory) {
        this.tagStory = tagStory;
    }

    public String getDate() {
        return DateStory;
    }

    public void setDate(String dateStory) {
        DateStory = dateStory;
    }

    public Boolean getCheckedItem() {
        return isItemChecked;
    }

    public void setCheckedItem(Boolean itemChecked) {
        isItemChecked = itemChecked;
    }
}
