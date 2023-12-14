package com.example.cw4;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


public class StoryEnt extends AppCompatActivity {
    // put textCortex API key here -> final String API_KEY = "";
    String url = "https://api.textcortex.com/v1/texts/social-media-posts";
    TextToSpeech tts;
    EditText keywords;
    ImageView bButton;
    TextView story;
    Button submit, find;
    private EditText edit;
    DHelper dhelper;
    SQLiteDatabase db;
    private boolean incSketch = false;
    private StoryAdapter adapter4;
    private TextView selectItemsView;
    private ListView lv4;
    private ArrayList<StoryItems> storydata;
    CheckBox incSketchBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        stopService(new Intent(this, MusicService.class));

        submit = findViewById(R.id.submit);
        bButton = findViewById(R.id.back);
        find = findViewById(R.id.Find);
        story = findViewById(R.id.Story);
        incSketchBox = findViewById(R.id.includesketch);
        selectItemsView = findViewById(R.id.Selectedtags);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String context = selectItemsView.getText().toString();
                    String keywords = selectItemsView.getText().toString();
                    makeHttpRequest(context, keywords, story);
                    tts = new TextToSpeech(StoryEnt.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                tts.setLanguage(Locale.ENGLISH);
                                tts.setPitch(1.02F);
                                tts.setLanguage(Locale.US);
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e("error", e.toString());
                }
            }
        });

        lv4 = findViewById(R.id.lv4);
        storydata = new ArrayList<>();
        adapter4 = new StoryAdapter(this, R.layout.story_list_item, storydata, selectItemsView);
        SparseBooleanArray checkedItems = adapter4.getCheckedItems();
        adapter4.notifyDataSetChanged();
        lv4.setAdapter(adapter4);
        dhelper = new DHelper(this);
        db = dhelper.getWritableDatabase();
        fetchAndCombine();
        adapter4.notifyDataSetChanged();

        bButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StoryEnt.this, TypeTagger.class));
            }
        });
        incSketchBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            incSketch = isChecked;
            storydata.clear();
            fetchAndCombine();
            adapter4.notifyDataSetChanged();
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performStorySearch();
            }
        });
    }
    private void combineResults(Cursor imagecursor, Cursor scursor) {
        storydata.clear();

        results(imagecursor, "IMAGE", "NAME", "IMAGE_DATE");

        if (incSketch && scursor != null) {
            results(scursor, "SKETCH_IMAGE", "SKETCH_NAME", "SKETCH_DATE");
        }
    }
    private void performStorySearch() {
        String searchName = find.getText().toString();
        String userSelection, sketchSelection;
        String[] userSelectionArgs, sketchSelectionArgs;
        if (searchName.isEmpty()) {
            userSelection = "NAME IS NULL OR NAME = '' ";
            userSelectionArgs = null;
        } else {
            userSelection = "NAME LIKE ?";
            userSelectionArgs = new String[]{"%" + searchName + "%"};
        }
        Cursor imagecursor = db.query("IMAGES", null, userSelection,
                userSelectionArgs, null, null, "IMAGE_DATE DESC LIMIT 40");

        if (searchName.isEmpty()) {
            sketchSelection = "SKETCH_NAME IS NULL OR SKETCH_NAME = '' ";
            sketchSelectionArgs = null;
        } else {
            sketchSelection = "SKETCH_NAME LIKE ?";
            sketchSelectionArgs = new String[]{"%" + searchName + "%"};
        }
        Cursor scursor = null;
        if (incSketch) {
            scursor = db.query("SKETCHES", null, sketchSelection,
                    sketchSelectionArgs, null, null, "SKETCH_DATE DESC LIMIT 40");
        }

        combineResults(imagecursor, scursor);

        if (imagecursor != null) {
            imagecursor.close();
        }
        if (scursor != null) {
            scursor.close();
        }
        ((StoryAdapter) lv4.getAdapter()).notifyDataSetChanged();
    }

    private void fetchAndCombine() {

        Cursor imagecursor = db.query(
                "IMAGE",
                null,
                null,
                null,
                null,
                null,
                "IMAGE_DATE DESC "
        );

        Cursor scursor = null;
        if (incSketch) {
            scursor = db.query(
                   "SKETCHES",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "SKETCH_DATE DESC "
            );
        }

        combineResults(imagecursor, scursor);

        if (imagecursor != null) {
            imagecursor.close();
       }
        if (scursor != null) {
            scursor.close();
       }
    }

    public void results(Cursor cursor,String imageCol, String nameCol, String timeStamp ) {
        int count = 0;
        if (cursor != null) {
            if(incSketch ==false) {
                storydata.clear();
            }
            count = cursor.getCount();
        }
        if (count == 0) {
            Toast.makeText(this, "No photo image found", Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < 100; i++) {
            if (i < count) {
                cursor.moveToPosition(i);

                int imageIndex = cursor.getColumnIndex(imageCol);
                int tagIndex = cursor.getColumnIndex(nameCol);
                int dateInd = cursor.getColumnIndex(timeStamp);

                if (imageIndex != -1) {
                    try {
                        byte[] imageBytes = cursor.getBlob(imageIndex);
                        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        String tagName = cursor.getString(tagIndex);
                        String timestamper = cursor.getString(dateInd);

                        StoryItems  item3 = new StoryItems(image, tagName, timestamper,false);
                        storydata.add(item3);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }
    void makeHttpRequest(String context, String keywords,final TextView story ) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("context", context);
        data.put("max_tokens", 512);
        data.put("mode", "twitter");
        data.put("model", "chat-sophos-1");

        String[] keyArray = keywords.split(",");
        data.put("keywords", new JSONArray(Arrays.asList(keyArray)));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    JSONArray outputs = data.getJSONArray("outputs");
                    JSONObject firstOutput = outputs.getJSONObject(0);
                    String storyText = firstOutput.getString("text");

                    story.setText(storyText);
                    if (tts != null)
                    {
                        tts.speak(storyText, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    Log.d("success ", response.toString());
                } catch (JSONException e) {
                    Log.e("error", "Error parsing");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("errors", new String(error.networkResponse.data));
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + API_KEY);
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this) ;
        requestQueue.add(request);
    }
}
