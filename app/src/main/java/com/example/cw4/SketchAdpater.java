package com.example.cw4;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
public class SketchAdpater extends ArrayAdapter<SketchItem> {

    SketchAdpater(Context context, int resource, ArrayList<SketchItem> objects) {
        super(context, resource, objects);}
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.sketch_list_item, parent, false);
        }

        SketchItem currentsketchItem = getItem(position);
        ImageView sketchImage = convertView.findViewById(R.id.sketchImage);
        TextView sketchName = convertView.findViewById(R.id.sketchTag);
        TextView sketchDate=convertView.findViewById(R.id.sketchDate);
        sketchImage.setImageBitmap(currentsketchItem.getSketch());
        sketchName.setText(currentsketchItem.getSketchName());
        sketchDate.setText(currentsketchItem.getSketchDate());
        return convertView;
    }
}


