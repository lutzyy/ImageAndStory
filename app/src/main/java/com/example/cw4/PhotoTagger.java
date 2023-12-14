package com.example.cw4;


import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterView;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;


public class PhotoTagger extends AppCompatActivity {
    // put googleVision API key here -> final String API_KEY = "";
    ImageFilterView bButton;
    Bitmap tempBitMap;
    private ImageView tempImgView;
    Button cam,save,search,autotag;
    private EditText tagEdit, searchTag;
    DHelper dhelper;
    SQLiteDatabase db;
    private FoodListAdapter adapter3;
    private ListView lv3;
    private ArrayList<FoodItem> pdata;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        startService(new Intent(this, MusicService.class));

        initializeViews();
        setClickListeners();
    }
        private void initializeViews() {
            tempImgView = findViewById(R.id.placeholder);
            tagEdit = findViewById(R.id.tagEdit);
            searchTag = findViewById(R.id.searchedt);
            autotag = findViewById(R.id.autotag);
            cam = findViewById(R.id.camera);
            bButton = findViewById(R.id.back);

            dhelper = new DHelper(this);
            db = dhelper.getWritableDatabase();

            pdata = new ArrayList<>();
            adapter3 = new FoodListAdapter(this, R.layout.list_item, pdata);
            lv3 = findViewById(R.id.mylist);
            lv3.setAdapter(adapter3);
            save = findViewById(R.id.save);
            search = findViewById(R.id.search);
        }

        private void setClickListeners() {
            bButton.setOnClickListener(view -> {
                startActivity(new Intent(PhotoTagger.this, TypeTagger.class));
                finish();
            });

            cam.setOnClickListener(view -> {
                Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(openCamera, 1);
            });

            save.setOnClickListener(view -> {
                save();
                pdata.clear();
                showLatestImages1();
            });

            search.setOnClickListener(view -> search());

            autotag.setOnClickListener(view -> {
                VisionTask visionTask = new VisionTask();
                visionTask.execute();
            });
        }



        class VisionTask extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String description = myVisionTester(tempBitMap);
                    return description;
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
                    Toast.makeText(PhotoTagger.this, "Error while tag", Toast.LENGTH_SHORT).show();
                }
            }
        }

    private void showLatestImages1() {
        // query for the latest sketches from the "IMAGE" table
        Cursor cursor = db.query("IMAGE", null, null, null, null, null, "IMAGE_DATE DESC LIMIT 40");

        // query for the latest images
        pdata.clear(); // Clear existing data
        int count=0;
        if (cursor != null) {
            count = cursor.getCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);

                    int imageIndex = cursor.getColumnIndex("IMAGE");
                    int tagIndex = cursor.getColumnIndex("NAME");
                    int dateIndex = cursor.getColumnIndex("IMAGE_DATE");

                    if (imageIndex != -1) {
                        try {
                            byte[] imgByte = cursor.getBlob(imageIndex);
                            String tagInd = cursor.getString(tagIndex);
                            Bitmap img = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                            String timestamp = cursor.getString(dateIndex);
                            FoodItem item2 = new FoodItem(img, tagInd, timestamp);
                            pdata.add(item2);
                        } catch (Exception e) {
                            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            cursor.close();
        }
        adapter3.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            tempBitMap = (Bitmap) extra.get("data");
            tempImgView.setImageBitmap(tempBitMap);
        }
    }
    public void save() {
        String imgName = tagEdit.getText().toString();
        Bitmap b = ((BitmapDrawable) tempImgView.getDrawable()).getBitmap();

        if (b == null) {
            Toast.makeText(this, "bitmap null", Toast.LENGTH_SHORT).show();
        }
        else {
            SQLiteDatabase db = dhelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageBytes = stream.toByteArray();
            values.put("IMAGE", imageBytes);
            String Tag= tagEdit.getText().toString();
            values.put("NAME", Tag);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault()).format(new Date());
            values.put("IMAGE_DATE", timestamp);

            long newRowId = db.insert("IMAGE", null, values);
 }
        showLatestImages1();
    }
    String myVisionTester(Bitmap bitmap) throws IOException {
        //1. ENCODE image.
        //Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(imgs)).getBitmap();
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
        Log.v("MYTAG11", response.toPrettyString());

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
            byte[] img = new byte[(int) imageFile.length()];
            f.read(img);
            f.close();

            ContentValues values = new ContentValues();
            values.put("IMG", img);
            values.put("NAME", tags);
            values.put("IMAGE_DATE", date);
            db.insert("IMAGE", null, values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public String EditText() {
        return tagEdit.getText().toString();
    }
    public String searchTag() {
        return searchTag.getText().toString();
    }



    public void search() {
        String searchName = searchTag();
        String sketch_Sel;
        String[] sketch_SelArgs;
        if (searchName.isEmpty()) {
            sketch_Sel = "NAME IS NULL OR SKETCH_NAME = '' ";
            sketch_SelArgs = null;
        } else {
            sketch_Sel = "NAME LIKE ?";
            sketch_SelArgs = new String[]{"%" + searchName + "%"};
        }

        Cursor curs = db.query("IMAGE", null, sketch_Sel, sketch_SelArgs, null, null,"IMAGE_DATE DESC LIMIT 40");

        int count = 0;
        if (curs != null) {
            pdata.clear();
            count = curs.getCount();
        }
        if (count == 0) {
            Toast.makeText(this, "bo photo", Toast.LENGTH_SHORT).show();
        }

        int lim = Math.min(40, count);
        for (int i = 0; i < lim; i++){
            if (i < count) {
                curs.moveToPosition(i);
                int imgIndex = curs.getColumnIndex("IMAGE");
                int tagIndex = curs.getColumnIndex("NAME");
                int dateIndex = curs.getColumnIndex("IMAGE_DATE");
                if (imgIndex != -1) {
                    try {
                        byte[] imgBytes = curs.getBlob(imgIndex);
                        Bitmap img = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                        String tag = curs.getString(tagIndex);
                        String timestamp = curs.getString(dateIndex);
                        FoodItem  item2 = new FoodItem(img, tag, timestamp);
                        pdata.add(item2);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        if (curs != null) {
            curs.close();
        }
        adapter3 = new FoodListAdapter(this, R.layout.list_item, pdata);
        lv3.setAdapter(adapter3);
        adapter3.notifyDataSetChanged();
    }
//    public void onBackButtonClick(View view) {
//        setContentView(R.layout.activity_home);
//    }

}