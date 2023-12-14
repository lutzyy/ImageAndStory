package com.example.cw4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;


public class TypeTagger extends AppCompatActivity {
    Button pTagger,sTagger,Story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pTagger = findViewById(R.id.pTagger);
        sTagger = findViewById(R.id.sTagger);
        Story = findViewById(R.id.Story);

       sTagger.setOnClickListener(view -> startActivity(new Intent(TypeTagger.this, DrawTagger.class)));
       pTagger.setOnClickListener(view -> startActivity(new Intent(TypeTagger.this, PhotoTagger.class)));
       Story.setOnClickListener(view -> startActivity(new Intent(TypeTagger.this, StoryEnt.class)));
    }
}