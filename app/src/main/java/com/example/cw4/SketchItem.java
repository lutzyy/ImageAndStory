package com.example.cw4;

import android.graphics.Bitmap;

public class SketchItem {
    Bitmap sketch;
    String sketchTag,sketchDate;

    public SketchItem(Bitmap sketch, String sketchTag, String sketchDate) {
        this.sketch = sketch;
        this.sketchTag = sketchTag;
        this.sketchDate = sketchDate;
    }

    public Bitmap getSketch() {
        return sketch;
    }

    public String getSketchName() {
        return sketchTag;
    }

    public String getSketchDate() {
        return sketchDate;
    }
}


