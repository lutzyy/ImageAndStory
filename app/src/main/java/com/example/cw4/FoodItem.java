package com.example.cw4;

import android.graphics.Bitmap;

public class FoodItem {
    Bitmap imageResource;
    String name;

    String date;

    public FoodItem(Bitmap imageResource, String name, String date) {
        this.imageResource = imageResource;
        this.name = name;
        this.date=date;
    }

    public Bitmap getImageResource() {

        return imageResource;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}


