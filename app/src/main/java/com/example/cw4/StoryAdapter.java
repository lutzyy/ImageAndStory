package com.example.cw4;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StoryAdapter extends ArrayAdapter<StoryItems> {
    private List<StoryItems> list;

    TextView textView;

    private SparseBooleanArray array;


    public StoryAdapter(@NonNull Context context, int resource, @NonNull List<StoryItems> objects, TextView textView) {
        super(context, resource, objects);
        list = objects;
        this.textView = textView;

        array = new SparseBooleanArray();


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        StoryItems item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.story_list_item, parent, false);
        }


        ImageView image = convertView.findViewById(R.id.image);
        TextView tag = convertView.findViewById(R.id.tag);
        TextView date = convertView.findViewById(R.id.date);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);

        if (item != null) {
            image.setImageBitmap(item.getImageResource());
            tag.setText(item.getTag());
            date.setText(item.getDate());



            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setCheckedItem(isChecked);

                array.put(position, isChecked);

                updateSelectedItemsTextView();





            });

        }
        checkBox.setChecked(array.get(position, false));

        return convertView;
    }


    public SparseBooleanArray getCheckedItems() {
        return array;
    }


    public void updateSelectedItemsTextView() {
        StringBuilder names = new StringBuilder();

        for (StoryItems item : list) {
            if (item.getCheckedItem()) {
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(item.getTag());
            }
        }

        textView.setText(names.toString());
    }



}


