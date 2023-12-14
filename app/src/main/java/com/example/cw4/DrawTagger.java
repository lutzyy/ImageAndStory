package com.example.cw4;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterView;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

public class DrawTagger extends AppCompatActivity {
    // put googleVision API key here -> final String API_KEY = "";
    ImageFilterView bButton;
    private ImageView tempImgView;
    private MyDrawingArea drawView;
    private SketchAdpater adapter2;
    private EditText tagEdit, searchTag;
    Bitmap temp;
    Button cam, red, white, blue,save, Search, autotag;
    SQLiteDatabase db;
    private ListView slv2;
    DHelper dhelper;
    private ArrayList<SketchItem> sketData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        startService(new Intent(this, MusicService.class));

        initializeViews();
        setClickListeners();

        dhelper = new DHelper(this);
        db = dhelper.getWritableDatabase();

        autotag.setOnClickListener(v -> {
            VisionTask1 visionTask = new VisionTask1();
            visionTask.execute();
        });
    }

    private void initializeViews() {
        bButton = findViewById(R.id.back);
        drawView = findViewById(R.id.cusview);
        tagEdit = findViewById(R.id.tagEdit);
        searchTag = findViewById(R.id.searchedt);
        red = findViewById(R.id.red);
        blue = findViewById(R.id.blue);
        white = findViewById(R.id.white);
        save = findViewById(R.id.save);
        Search = findViewById(R.id.search);
        autotag = findViewById(R.id.autotag);

        sketData = new ArrayList<>();
        adapter2 = new SketchAdpater(this, R.layout.sketch_list_item, sketData);
        slv2 = findViewById(R.id.mylist2);
        slv2.setAdapter(adapter2);
    }

    private void setClickListeners() {
        bButton.setOnClickListener(view -> {
            startActivity(new Intent(DrawTagger.this, TypeTagger.class));
            finish();
        });

        white.setOnClickListener(v -> drawView.SetColor(Color.WHITE));

        blue.setOnClickListener(v -> drawView.SetColor(Color.BLUE));

        red.setOnClickListener(v -> drawView.SetColor(Color.RED));

        save.setOnClickListener(view -> {
            Bitmap b = drawView.getBitmap();
            drawView.clear();
            sketData.clear();
            showLatestImages();
            save(b);
        });

        Search.setOnClickListener(view -> search2());
    }

    private class VisionTask1 extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                return myVisionTester();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String description) {
            if (description != null) {
                tagEdit.setText(description);
            } else {
                Toast.makeText(DrawTagger.this, "Error while tagging", Toast.LENGTH_SHORT).show();
            }
        }
    }

        public void clear(View view) {
        MyDrawingArea mcas = findViewById(R.id.cusview);
        mcas.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            Bundle extra = data.getExtras();
            temp = (Bitmap) extra.get("data");
            tempImgView.setImageBitmap(temp);
        }
    }
    public void save(Bitmap b) {
        if (b == null) {
            Toast.makeText(this, "sketch not found", Toast.LENGTH_SHORT).show();
            return;
        }
else {
    SQLiteDatabase db = dhelper.getWritableDatabase();
    ContentValues values = new ContentValues();

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    b.compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] imageBytes = stream.toByteArray();
    values.put("SKETCH_IMAGE", imageBytes);

    String sketchName = tagEdit.getText().toString();
    values.put("SKETCH_NAME", sketchName);

    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault()).format(new Date());
    Log.i("MYTAGformatdate", "time go");
    values.put("SKETCH_DATE", timestamp);

    long newRowId = db.insert("SKETCHES", null, values);

    Toast.makeText(this, "Sketch saved to database with ID: " + newRowId, Toast.LENGTH_SHORT).show();
    }
        showLatestImages();
    }
        private void showLatestImages() {
            Cursor cursor = db.query("SKETCHES", null, null,
                    null, null, null,"SKETCH_DATE DESC LIMIT 40");

            sketData.clear();
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        cursor.moveToPosition(i);

                        int dateIndex = cursor.getColumnIndex("SKETCH_DATE");
                        int imageIndex = cursor.getColumnIndex("SKETCH_IMAGE");
                        int tagIndex = cursor.getColumnIndex("SKETCH_NAME");

                        if (imageIndex != -1) {
                            try {
                                byte[] imageBytes = cursor.getBlob(imageIndex);
                                Bitmap img = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                String tagNam = cursor.getString(tagIndex);
                                String dateTime = cursor.getString(dateIndex);

                                SketchItem item = new SketchItem(img, tagNam, dateTime);
                                sketData.add(item);
                            } catch (Exception e) {
                                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                cursor.close();
            }
            adapter2.notifyDataSetChanged();
        }

    private String myVisionTester() throws IOException {
        //1. ENCODE image.
        Bitmap bitmap = drawView.getBitmap();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bout);
        Image myimage = new Image();
        myimage.encodeContent(bout.toByteArray());
        //2. PREPARE AnnotateImageRequest
        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
        annotateImageRequest.setImage(myimage);
        Feature f = new Feature();
        f.setType("LABEL_DETECTION");
        f.setMaxResults(5);
        List<Feature> lf = new ArrayList<Feature>();
        lf.add(f);
        annotateImageRequest.setFeatures(lf);
        //3.BUILD the Vision
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(new VisionRequestInitializer(API_KEY));
        Vision vision = builder.build();
        //4. CALL Vision.Images.Annotate
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        List<AnnotateImageRequest> list = new ArrayList<AnnotateImageRequest>();
        list.add(annotateImageRequest);
        batchAnnotateImagesRequest.setRequests(list);
        Vision.Images.Annotate task = vision.images().annotate(batchAnnotateImagesRequest);
        BatchAnnotateImagesResponse response = task.execute();
        Log.v("MYTAG22", response.toPrettyString());


        List<EntityAnnotation> tags = response.getResponses().get(0).getLabelAnnotations();


        List<String> toptags = new ArrayList<>();
        for (EntityAnnotation labelAnnotation : tags) {
            if (labelAnnotation.getScore() >= 0.85) {
                toptags.add(labelAnnotation.getDescription());
            }
        }


        String description;
        if (toptags.isEmpty()) {
            if (!tags.isEmpty())
                description = tags.get(0).getDescription();
            else {

                description = "No tags available";
            }
        } else {

            description = TextUtils.join(", ", toptags);
        }

        Log.v("MYTAG", description);

        return description;
    }
    public void update(File imageFile, String tags, String date) throws SQLiteException {
        try {
            FileInputStream f = new FileInputStream(imageFile);
            byte[] image = new byte[(int) imageFile.length()];
            f.read(image);
            f.close();

            ContentValues values = new ContentValues();
            values.put("SKETCH_IMAGE", image);
            values.put("SKETCH_NAME", tags);
            values.put("SKETCH_DATE", date);
            db.insert("SKETCHES", null, values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String EditText() {
        return tagEdit.getText().toString();
    }
    public String searchTag() {
        return searchTag.getText().toString().trim();
    }


    public void search2() {
        String searchNam = searchTag();
        String sketch_Sel;
        String[] sketch_SelArgs;

        if (searchNam.isEmpty()) {
            sketch_Sel = "SKETCH_NAME IS NULL OR SKETCH_NAME = '' ";
            sketch_SelArgs = null;
        } else {
            sketch_Sel = "SKETCH_NAME LIKE ?";
            sketch_SelArgs = new String[]{"%" + searchNam + "%"};
        }
        Cursor cursor = db.query("sketches", null, sketch_Sel,
                sketch_SelArgs, null, null,"SKETCH_DATE" + " DESC LIMIT 40");

        int count = 0;
        if (cursor != null) {
            sketData.clear();
            count = cursor.getCount();
        }
        if (count == 0) {
            Toast.makeText(this, "No skimage found", Toast.LENGTH_SHORT).show();
        }

        int lim = Math.min(40, count);
        for (int i = 0; i < lim; i++){
            if (i < count) {
                cursor.moveToPosition(i);

                int dateIndex = cursor.getColumnIndex("SKETCH_DATE");
                int imgIndex = cursor.getColumnIndex("SKETCH_IMAGE");
                int tagIndex = cursor.getColumnIndex("SKETCH_NAME");

                if (imgIndex != -1) {
                    try {
                        byte[] imgBytes = cursor.getBlob(imgIndex);
                        Bitmap img = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                        String tagNam = cursor.getString(tagIndex);
                        String datetime = cursor.getString(dateIndex);

                        SketchItem item = new SketchItem(img, tagNam, datetime);
                        sketData.add(item);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        adapter2 = new SketchAdpater(this, R.layout.sketch_list_item, sketData);
        slv2.setAdapter(adapter2);
        Log.i("MYTAGhere", "working");
        adapter2.notifyDataSetChanged();


    }
}